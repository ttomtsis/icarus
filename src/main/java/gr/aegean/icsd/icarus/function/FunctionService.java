package gr.aegean.icsd.icarus.function;

import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import gr.aegean.icsd.icarus.util.annotations.GithubUrl.GithubUrl;
import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.entity.InvalidEntityConfigurationException;
import gr.aegean.icsd.icarus.util.interfaces.UtilitiesInterface;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import gr.aegean.icsd.icarus.util.services.FileService;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Service
@Transactional
@Validated
public class FunctionService implements UtilitiesInterface {


    private final FunctionRepository functionRepository;
    private final FileService fileService;



    public FunctionService(FunctionRepository repository, FileService fileService) {
        this.functionRepository = repository;
        this.fileService = fileService;
    }



    public Function createFunction(@NotNull Function newFunction, MultipartFile functionSource)
            throws IOException {

        // Source code not provided
        if (StringUtils.isBlank(newFunction.getGithubURL()) && functionSource == null) {

            throw new InvalidEntityConfigurationException(Function.class,
                    "No source code has been provided for the function");
        }

        // Github url provided
        else if (StringUtils.isNotBlank(newFunction.getGithubURL()) &&
                functionSource == null) {

            cloneFunctionSourceFromRepository(newFunction, newFunction.getGithubURL());
        }

        // Source code provided in request
        else if (StringUtils.isBlank(newFunction.getGithubURL()) &&
                functionSource != null) {

            saveFunctionSourceCode(newFunction, functionSource);
        }

        // Both url and source code provided, will persist the source code on the request
        else if (StringUtils.isNotBlank(newFunction.getGithubURL()) &&
                    functionSource != null){

            saveFunctionSourceCode(newFunction, functionSource);
        }

        return functionRepository.save(newFunction);
    }


    public void deleteFunction(@NotNull @Positive Long functionId)
            throws IOException {

        Function existingFunction = checkIfFunctionExists(functionId);

        deleteFunctionSourceCode(existingFunction);

        functionRepository.delete(existingFunction);
    }


    public void updateFunction(@NotNull @Positive Long functionId,
                                FunctionModel model, MultipartFile newFunctionSource)
            throws IOException {

        Function existingFunction = checkIfFunctionExists(functionId);

        if (model != null) {

            setIfNotBlank(existingFunction::setName, model.getName());
            setIfNotBlank(existingFunction::setDescription, model.getDescription());
            setIfNotBlank(existingFunction::setFunctionHandler, model.getFunctionHandler());
        }

        if (newFunctionSource != null && model != null && StringUtils.isBlank(model.getGithubURL())) {

            deleteFunctionSourceCode(existingFunction);
            saveFunctionSourceCode(existingFunction, newFunctionSource);
        }
        else if (newFunctionSource == null && model != null && !StringUtils.isBlank(model.getGithubURL())) {

            deleteFunctionSourceCode(existingFunction);
            cloneFunctionSourceFromRepository(existingFunction, model.getGithubURL());
        }
        else if (newFunctionSource != null && model != null && StringUtils.isNotBlank(model.getGithubURL())) {

            deleteFunctionSourceCode(existingFunction);
            saveFunctionSourceCode(existingFunction, newFunctionSource);
        }

        functionRepository.save(existingFunction);
    }


    public Function getFunction(@NotNull @Positive Long functionId) {

        return checkIfFunctionExists(functionId);
    }


    private Function checkIfFunctionExists(Long functionId) {

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();

        return functionRepository.findFunctionByIdAndAuthor(functionId, loggedInUser)
                .orElseThrow( () -> new EntityNotFoundException(Function.class, functionId));
    }


    private void saveFunctionSourceCode(Function function,
                                        MultipartFile functionSourceFile)
            throws IOException {

        String functionSourceFileName = function.getName() + ".zip";

        fileService.createDirectory(getFunctionSourceDirectory());

        fileService.saveFile(getFunctionSourceDirectory(), functionSourceFileName, functionSourceFile);
        fileService.validateZipFile(getFunctionSourceDirectory() + File.separator + functionSourceFileName);

        function.setFunctionSource(functionSourceFile.getBytes());
        function.setFunctionSourceFileName(functionSourceFileName);
    }


    private void saveFunctionSourceCode(Function function,
                                        File functionSourceFile)
            throws IOException {

        fileService.validateZipFile(functionSourceFile.getPath());

        function.setFunctionSource(Files.readAllBytes(Path.of(functionSourceFile.getPath())));
        function.setFunctionSourceFileName(function.getName() + ".zip");
    }


    private void deleteFunctionSourceCode(Function function)
            throws IOException {

        String functionSourceDirectory = getFunctionSourceDirectory();
        String functionSourceFileName = function.getName() + ".zip";

        String absoluteFunctionSourceFilePath = functionSourceDirectory + File.separator + functionSourceFileName;

        if (Files.exists(Path.of(absoluteFunctionSourceFilePath))) {
            fileService.deleteFile(absoluteFunctionSourceFilePath);
        }

    }


    private void cloneFunctionSourceFromRepository(Function function,
                                                   @GithubUrl String repositoryUrl) {

        // Specify a temporary directory to store cloned files
        String tempDirectory = getFunctionSourceDirectory() + File.separator + "temp";
        File repositoryOutputDirectory = new File(tempDirectory);

        // Clone repository into the temporary directory
        try (
            Git g = Git.cloneRepository()
                    .setURI(repositoryUrl)
                    .setDirectory(repositoryOutputDirectory)
                    .call()
        ) {

            // Specify the absolute file path where the source code of the function will be located, in zip format
            String sourceOutputDirectory = getFunctionSourceDirectory() + File.separator + function.getName() + ".zip";
            File outputDirectory = new File(sourceOutputDirectory);

            // Compress the cloned repository files into a zip
            fileService.compressDirectoryToZip(repositoryOutputDirectory, outputDirectory);

            // Save the source code in the database
            saveFunctionSourceCode(function, outputDirectory);

        }

        catch (GitAPIException | IOException e) {
            throw new InvalidEntityConfigurationException(Function.class, "Exception when cloning function's source " +
                    "code from repository", e);
        }

        // Delete the temporary directory and all of it's contents
        finally {
            Git.shutdown();
            fileService.deleteDirectory(repositoryOutputDirectory.getPath());
        }

    }


}
