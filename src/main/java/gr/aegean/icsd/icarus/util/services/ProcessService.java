package gr.aegean.icsd.icarus.util.services;

import gr.aegean.icsd.icarus.util.exceptions.async.StackDeploymentException;
import gr.aegean.icsd.icarus.util.exceptions.async.AsyncExecutionFailedException;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

        log.warn("executing command: {} at directory: {}",
                Arrays.toString(commands), commandDirectory.getPath());

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
            throw new AsyncExecutionFailedException(ex);
        }

    }

    public String runCommand(@NotNull String ... command) throws InterruptedException, IOException {

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(command);

        log.info("Executing command: {}", Arrays.toString(command));

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line + "\n");
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            log.error("Command: {} was not completed successfully", Arrays.toString(command));
        }

        return output.toString();
    }


}
