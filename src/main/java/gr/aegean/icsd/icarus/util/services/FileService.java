package gr.aegean.icsd.icarus.util.services;

import gr.aegean.icsd.icarus.function.FunctionService;
import gr.aegean.icsd.icarus.util.exceptions.async.TestExecutionFailedException;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;


@Service
@Validated
public class FileService {


    public void deleteFile(String fileDirectory) throws IOException {

        Path functionSourceFilePath = Paths.get(fileDirectory);

        if (!Files.exists(functionSourceFilePath)) {
            LoggerFactory.getLogger(FunctionService.class).warn("Failed to delete file: {}\n" +
                    " Specified file does not exist", fileDirectory);
        }

        else {
            Files.delete(functionSourceFilePath);
        }

    }


    public void deleteDirectory(@NotBlank String dir) {

        try {

            Files.walkFileTree(Path.of(dir), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });

        }
        catch (IOException ex) {
            throw new TestExecutionFailedException(ex);
        }

    }


    public void saveFile(String fileDirectory, String fileName, MultipartFile file) throws IOException {

        try {

            Path dirPath = Paths.get(fileDirectory);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            byte[] bytes = file.getBytes();
            Path filePath = Paths.get(fileDirectory + "\\" + fileName);
            Files.write(filePath, bytes);

        } catch (IOException ex) {

            String errorMessage = "Error when saving File to: " + fileDirectory + "\n" +
                    Arrays.toString(ex.getStackTrace());

            throw new IOException(errorMessage, ex);
        }

    }


}
