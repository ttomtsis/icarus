package gr.aegean.icsd.icarus.function;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.user.IcarusUser;
import gr.aegean.icsd.icarus.util.exceptions.function.FunctionNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.test.TestNotFoundException;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Consumer;


@Service
@Transactional
@Validated
public class FunctionService {


    private final TestRepository testRepository;
    private final FunctionRepository functionRepository;

    @Value("${security.users.functionSourcesDirectory}")
    private String functionSourcesDirectory;


    public FunctionService(TestRepository testRepository, FunctionRepository repository) {
        this.testRepository = testRepository;
        this.functionRepository = repository;
    }



    public Function createFunction(@NotNull Function newFunction, @NotNull MultipartFile functionSource,
                                   @NotNull @Positive Long testId)
            throws IOException {

        // TODO: See issue #1 at GitHub
        Test associatedTest = checkIfTestExists(testId);

        setFunctionSourceDirectoryAndSourceName(newFunction, functionSource);
        Function savedFunction = functionRepository.save(newFunction);

        associatedTest.setTargetFunction(savedFunction);
        testRepository.save(associatedTest);

        return savedFunction;
    }


    public void deleteFunction(@NotNull @Positive Long testId, @NotNull @Positive Long functionId)
            throws IOException {

        checkIfTestExists(testId);

        Function existingFunction = checkIfFunctionExists(functionId);

        deleteFunctionSource(existingFunction);
        functionRepository.delete(existingFunction);
    }


    public void updateFunction(@NotNull @Positive Long testId, @NotNull @Positive Long functionId,
                                FunctionModel model, MultipartFile newFunctionSource)
            throws IOException {

        checkIfTestExists(testId);

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


    public Function getFunction(@NotNull @Positive Long testId, @NotNull @Positive Long functionId) {

        checkIfTestExists(testId);

        return checkIfFunctionExists(functionId);
    }



    private void setIfNotBlank(Consumer<String> setter, String value) {

        if (StringUtils.isNotBlank(value)) {
            setter.accept(value);
        }
    }


    private Test checkIfTestExists(Long associatedTestId) {

        return testRepository.findTestByIdAndCreator(associatedTestId, UserUtils.getLoggedInUser())
                .orElseThrow( () -> new TestNotFoundException(associatedTestId));
    }


    private Function checkIfFunctionExists(Long functionId) {

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();

        return functionRepository.findFunctionByIdAndAuthor(functionId, loggedInUser)
                .orElseThrow( () -> new FunctionNotFoundException(functionId));
    }


    private void setFunctionSourceDirectoryAndSourceName(@NotNull Function function,
                                                         @NotNull MultipartFile functionSourceFile)
            throws IOException {

        String functionSourceDirectory = functionSourcesDirectory + "\\Functions\\" + UserUtils.getUsername();
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

    private void deleteFunctionSource(Function function) throws IOException {

        String functionSourceDirectory = functionSourcesDirectory + "\\Functions\\" + UserUtils.getUsername();
        String functionSourceFileName = function.getName() + ".zip";

        Path functionSourceFilePath = Paths.get(functionSourceDirectory + "\\" + functionSourceFileName);

        if (!Files.exists(Paths.get(functionSourceDirectory))) {
            throw new IOException("Function's source directory does not exist");
        }

        if (!Files.exists(functionSourceFilePath)) {
            throw new IOException("Function's source code does not exist");
        }

        Files.delete(functionSourceFilePath);
    }


}
