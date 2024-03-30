package gr.aegean.icsd.icarus.util.services;

import gr.aegean.icsd.icarus.function.Function;
import gr.aegean.icsd.icarus.util.annotations.ValidFilePath.ValidFilePath;
import gr.aegean.icsd.icarus.util.exceptions.async.AsyncExecutionFailedException;
import gr.aegean.icsd.icarus.util.exceptions.entity.InvalidEntityConfigurationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
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
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


@Service
@Validated
public class FileService {


    private static final Logger log = LoggerFactory.getLogger(FileService.class);



    public void deleteFile(String fileLocation) throws IOException {

        Path filePath = Paths.get(fileLocation);

        if (!Files.exists(filePath)) {
            log.warn("Failed to delete file: {}\n" +
                    " Specified file does not exist", fileLocation);
        }

        else {
            try {
                Files.delete(filePath);
            }
            catch (RuntimeException ex) {
                log.warn("Unable to access: {}\n Will not delete", filePath);
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


    /**
     * Saves a MultipartFile in a specified Directory
     *
     * @param fileDirectory Directory that the file will be saved at
     * @param fileName Name that will be given to the saved file
     * @param file MultipartFile that will be saved
     *
     * @throws IOException If an issue is encountered during the saving of the file
     */
    public void saveFile(@ValidFilePath String fileDirectory, @NotBlank String fileName, @NotNull MultipartFile file)
            throws IOException {

        try {

            Path dirPath = Paths.get(fileDirectory);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            byte[] bytes = file.getBytes();
            Path filePath = Paths.get(fileDirectory + File.separator + fileName);
            Files.write(filePath, bytes);

        } catch (IOException ex) {

            String errorMessage = "Error when saving File to: " + fileDirectory + "\n" +
                    Arrays.toString(ex.getStackTrace());

            throw new IOException(errorMessage, ex);
        }

    }


    /**
     * Checks if target file is a zip file and can be opened without errors
     *
     * @param absoluteFilePath Path to the file
     *
     * @throws InvalidEntityConfigurationException If the file is not a valid zip file
     */
    public void validateZipFile(@NotBlank String absoluteFilePath) {

        try (ZipFile zipfile = new ZipFile(absoluteFilePath)) {

        } catch (IOException e) {
            throw new InvalidEntityConfigurationException(Function.class,
                    "Function's Source Code is not a valid zip file", e);
        }


    }


    public void saveBytesAsZip(@NotNull byte[] bytes, @NotBlank String filePath)
            throws IOException {

        FileOutputStream fos = new FileOutputStream(filePath);

        fos.write(bytes);
        fos.close();
    }


    public void compressDirectoryToZip(@NotNull File sourceDirectory, @NotNull File outputDirectory)
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


    public void createDirectory(@NotBlank String directoryPath) {

        Path path = Paths.get(directoryPath);

        try {
            if(!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            log.error("Could not create directory: {}", directoryPath);
            throw new AsyncExecutionFailedException(e);
        }
    }


}
