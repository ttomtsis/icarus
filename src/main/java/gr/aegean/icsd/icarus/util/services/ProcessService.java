package gr.aegean.icsd.icarus.util.services;

import gr.aegean.icsd.icarus.util.exceptions.async.StackDeploymentException;
import gr.aegean.icsd.icarus.util.exceptions.async.TestExecutionFailedException;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


@Service
@Validated
public class ProcessService {


    private static final Logger log = LoggerFactory.getLogger(ProcessService.class);


    public void createProcess(@NotNull File commandDirectory, @NotNull String ... commands) {

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(commandDirectory);
        processBuilder.command(commands);

        String commandString = "_" + commands[1];

        log.warn("executing command: {}", Arrays.toString(commands));

        File outputFile = new File(processBuilder.directory().getPath() + commandString + "_output.txt");
        processBuilder.redirectOutput(outputFile);

        File errorFile = new File(processBuilder.directory().getPath() + commandString + "_error_output.txt");
        processBuilder.redirectError(errorFile);

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new StackDeploymentException(errorFile, exitCode, commands);
            }
        }
        catch (InterruptedException | IOException ex) {
            throw new TestExecutionFailedException(ex);
        }

    }


}
