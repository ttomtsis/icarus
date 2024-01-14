package gr.aegean.icsd.icarus.function;

import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import gr.aegean.icsd.icarus.util.services.FileService;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

        fileService.saveFile(functionSourceDirectory, functionSourceFileName, functionSourceFile);

        function.setFunctionSourceDirectory(functionSourceDirectory);
        function.setFunctionSourceFileName(functionSourceFileName);
    }

    private void deleteFunctionSource(Function function)
            throws IOException {

        String functionSourceDirectory = FUNCTION_SOURCES_DIRECTORY + "\\" + UserUtils.getUsername() + "\\Functions";
        String functionSourceFileName = function.getName() + ".zip";

        String functionSourceFilePath = functionSourceDirectory + "\\" + functionSourceFileName;

        fileService.deleteFile(functionSourceFilePath);
    }


}
