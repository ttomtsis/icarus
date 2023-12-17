package gr.aegean.icsd.icarus.util.exceptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class StackDeploymentException extends RuntimeException {

    public StackDeploymentException(File errorFile, String... command) {
        super("Exception occurred when running command: " + Arrays.toString(command)
                + "\nError file:  \n" + getErrorFileContents(errorFile));
    }


    private static String getErrorFileContents(File errorFile) {
        try {
            return new String(Files.readAllBytes(Paths.get(errorFile.getPath())));
        } catch (IOException e) {
            return "Unable to read error file: " + errorFile.getPath();
        }
    }


}
