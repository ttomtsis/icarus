package gr.aegean.icsd.icarus.function;

import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import gr.aegean.icsd.icarus.util.annotations.GithubUrl.GithubUrl;
import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.entity.FunctionConfigurationException;
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
import java.util.UUID;
import java.util.function.Consumer;

import static gr.aegean.icsd.icarus.IcarusConfiguration.FUNCTION_SOURCES_DIRECTORY;


@Service
@Transactional
@Validated
public class FunctionService {


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

            throw new FunctionConfigurationException("No source code has been provided for the function");
        }

        // Github url provided
        else if (StringUtils.isNotBlank(newFunction.getGithubURL()) &&
                functionSource == null) {

            cloneFunctionSourceFromRepository(newFunction, newFunction.getGithubURL());
        }

        // Source code provided in request
        else if (StringUtils.isBlank(newFunction.getGithubURL()) &&
                functionSource != null) {

            saveFunctionSourceFromRequest(newFunction, functionSource);
        }

        // Both url and source code provided, will persist the source code on the request
        else {

            saveFunctionSourceFromRequest(newFunction, functionSource);
        }

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
            setIfNotBlank(existingFunction::setFunctionHandler, model.getFunctionHandler());
        }

        if (newFunctionSource != null && model != null && StringUtils.isBlank(model.getGithubURL())) {

            deleteFunctionSource(existingFunction);
            saveFunctionSourceFromRequest(existingFunction, newFunctionSource);
        }
        else if (newFunctionSource == null && model != null && !StringUtils.isBlank(model.getGithubURL())) {

            deleteFunctionSource(existingFunction);
            cloneFunctionSourceFromRepository(existingFunction, model.getGithubURL());
        }
        else if (newFunctionSource != null && model != null && StringUtils.isNotBlank(model.getGithubURL())) {

            deleteFunctionSource(existingFunction);
            saveFunctionSourceFromRequest(existingFunction, newFunctionSource);
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


    private void saveFunctionSourceFromRequest(@NotNull Function function,
                                               @NotNull MultipartFile functionSourceFile)
            throws IOException {

        String functionSourceDirectory = getFunctionSourceDirectory();
        String functionSourceFileName = function.getName() + ".zip";

        fileService.saveFile(functionSourceDirectory, functionSourceFileName, functionSourceFile);

        function.setFunctionSourceDirectory(functionSourceDirectory);
        function.setFunctionSourceFileName(functionSourceFileName);
    }


    private void deleteFunctionSource(Function function)
            throws IOException {

        String functionSourceDirectory = getFunctionSourceDirectory();
        String functionSourceFileName = function.getName() + ".zip";

        String functionSourceFilePath = functionSourceDirectory + "\\" + functionSourceFileName;

        fileService.deleteFile(functionSourceFilePath);
    }


    private void cloneFunctionSourceFromRepository(@NotNull Function function,
                                                   @GithubUrl String repositoryUrl) {

        String tempDirectory = getFunctionSourceDirectory() + "\\temp\\temp-"
                + UUID.randomUUID().toString().substring(0, 8);

        File repositoryOutputDirectory = new File(tempDirectory);

        try (
            Git _ = Git.cloneRepository()
                    .setURI(repositoryUrl)
                    .setDirectory(repositoryOutputDirectory)
                    .call()
        ) {
            String functionSourceFileName = getFunctionSourceDirectory() + "\\" + function.getName() + ".zip";
            File functionSourceZipFile = new File(functionSourceFileName);

            fileService.saveFileAsZip(repositoryOutputDirectory, functionSourceZipFile);

            function.setFunctionSourceDirectory(getFunctionSourceDirectory());
            function.setFunctionSourceFileName(function.getName() + ".zip");
        }

        catch (GitAPIException | IOException e) {
            throw new FunctionConfigurationException("Exception when cloning function's source " +
                    "code from repository", e);
        }

        finally {
            Git.shutdown();
            fileService.deleteDirectory(repositoryOutputDirectory.getPath());
        }

        }



    private String getFunctionSourceDirectory() {
        return FUNCTION_SOURCES_DIRECTORY + "\\" + UserUtils.getUsername() + "\\Functions";
    }


}
