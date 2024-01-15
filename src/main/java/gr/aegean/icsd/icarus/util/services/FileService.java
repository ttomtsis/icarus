package gr.aegean.icsd.icarus.util.services;

import gr.aegean.icsd.icarus.function.FunctionService;
import gr.aegean.icsd.icarus.util.exceptions.async.AsyncExecutionFailedException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
@Validated
public class FileService {



    public void deleteFile(String fileLocation) throws IOException {

        Path filePath = Paths.get(fileLocation);

        if (!Files.exists(filePath)) {
            LoggerFactory.getLogger(FunctionService.class).warn("Failed to delete file: {}\n" +
                    " Specified file does not exist", fileLocation);
        }

        else {
            try {
                Files.delete(filePath);
            }
            catch (RuntimeException ex) {
                LoggerFactory.getLogger(FileService.class).warn("Unable to access: {}\n Will not delete", filePath);
                throw new AsyncExecutionFailedException(ex);
            }
        }

    }


    @Async
    public void deleteDirectory(@NotBlank String dir) {

        try {
            File directory = new File(dir);
            deleteDirectory(directory);

        }
        catch (RuntimeException ex) {

            throw new AsyncExecutionFailedException(ex);
        }

    }

    private void deleteDirectory(File file) {

        if (file.exists() && file.isDirectory()) {

            for (File subFile : file.listFiles()) {
                deleteDirectory(subFile);
            }
        }

        if (!file.delete()) {
            LoggerFactory.getLogger(FileService.class).warn("Could not delete file: {}", file.getPath());
        }
    }


    public void saveFile(String fileDirectory, String fileName, MultipartFile file)
            throws IOException {

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


    public void saveFileAsZip(@NotNull File sourceDirectory, @NotNull File outputDirectory)
            throws IOException {

        try (
                FileOutputStream fos = new FileOutputStream(outputDirectory);
                ZipOutputStream zos = new ZipOutputStream(fos)
        ) {

            Files.walkFileTree(sourceDirectory.toPath(), new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    zos.putNextEntry(new ZipEntry(sourceDirectory.toPath().relativize(file).toString()));
                    Files.copy(file, zos);
                    zos.closeEntry();

                    return FileVisitResult.CONTINUE;
                }

            });

        }

    }


}
