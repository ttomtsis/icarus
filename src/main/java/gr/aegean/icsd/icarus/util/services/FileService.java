package gr.aegean.icsd.icarus.util.services;

import gr.aegean.icsd.icarus.util.exceptions.async.TestExecutionFailedException;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;


@Service
@Validated
public class FileService {


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


}
