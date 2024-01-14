package gr.aegean.icsd.icarus.function;

import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Consumer;

import static gr.aegean.icsd.icarus.IcarusConfiguration.FUNCTION_SOURCES_DIRECTORY;


@Service
@Transactional
@Validated
public class FunctionService {


    private final FunctionRepository functionRepository;



    public FunctionService(FunctionRepository repository) {
        this.functionRepository = repository;
    }



    public Function createFunction(@NotNull Function newFunction, @NotNull MultipartFile functionSource)
            throws IOException {

        setFunctionSourceDirectoryAndSourceName(newFunction, functionSource);
        return functionRepository.save(newFunction);
    }


    public void deleteFunction(@NotNull @Positive Long functionId)
            throws IOException {

        Function existingFunction = checkIfFunctionExists(functionId);

        deleteFunctionSource(existingFunction);

        functionRepository.delete(existingFunction);
    }


    public void updateFunction(@NotNull @Positive Long functionId,
                                FunctionModel model, MultipartFile newFunctionSource)
            throws IOException {

        Function existingFunction = checkIfFunctionExists(functionId);

        if (model != null) {
            setIfNotBlank(existingFunction::setName, model.getName());
            setIfNotBlank(existingFunction::setDescription, model.getDescription());
            setIfNotBlank(existingFunction::setGithubURL, model.getGithubURL());
            setIfNotBlank(existingFunction::setFunctionHandler, model.getFunctionHandler());
        }

        if (newFunctionSource != null) {

            deleteFunctionSource(existingFunction);
            setFunctionSourceDirectoryAndSourceName(existingFunction, newFunctionSource);
        }

        functionRepository.save(existingFunction);
    }


    public Function getFunction(@NotNull @Positive Long functionId) {

        return checkIfFunctionExists(functionId);
    }



    private void setIfNotBlank(Consumer<String> setter, String value) {

        if (StringUtils.isNotBlank(value)) {
            setter.accept(value);
        }
    }

    private Function checkIfFunctionExists(Long functionId) {

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();

        return functionRepository.findFunctionByIdAndAuthor(functionId, loggedInUser)
                .orElseThrow( () -> new EntityNotFoundException(Function.class, functionId));
    }

    private void setFunctionSourceDirectoryAndSourceName(@NotNull Function function,
                                                         @NotNull MultipartFile functionSourceFile)
            throws IOException {

        String functionSourceDirectory = FUNCTION_SOURCES_DIRECTORY + "\\" + UserUtils.getUsername() + "\\Functions";
        String functionSourceFileName = function.getName() + ".zip";

        try {

            // Create function source directory if it does not exist
            Path dirPath = Paths.get(functionSourceDirectory);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // Save function source to the directory
            byte[] bytes = functionSourceFile.getBytes();
            Path filePath = Paths.get(functionSourceDirectory + "\\" + functionSourceFileName);
            Files.write(filePath, bytes);

            // Set function source directory and function source filename
            function.setFunctionSourceDirectory(functionSourceDirectory);
            function.setFunctionSourceFileName(functionSourceFileName);

        } catch (IOException ex) {

            String errorMessage = "Error when saving function's source code to: " + functionSourceDirectory + "\n" +
                    Arrays.toString(ex.getStackTrace());

            throw new IOException(errorMessage, ex);
        }
    }

    private void deleteFunctionSource(Function function)
            throws IOException {

        String functionSourceDirectory = FUNCTION_SOURCES_DIRECTORY + "\\" + UserUtils.getUsername() + "\\Functions";
        String functionSourceFileName = function.getName() + ".zip";

        Path functionSourceFilePath = Paths.get(functionSourceDirectory + "\\" + functionSourceFileName);

        if (!Files.exists(Paths.get(functionSourceDirectory))) {
            LoggerFactory.getLogger(FunctionService.class).warn("Source code directory" +
                    " of Function {} does not exist", function.getName());
        }

        else if (!Files.exists(functionSourceFilePath)) {
            LoggerFactory.getLogger(FunctionService.class).warn("Source code of Function {}" +
                    " does not exist", function.getName());
        }

        else {
            Files.delete(functionSourceFilePath);
        }

    }


}
