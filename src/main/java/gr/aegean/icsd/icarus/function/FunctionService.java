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
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.function.Consumer;


@Service
@Transactional
@Validated
public class FunctionService {


    private final TestRepository testRepository;
    private final FunctionRepository functionRepository;



    public FunctionService(TestRepository testRepository, FunctionRepository repository) {
        this.testRepository = testRepository;
        this.functionRepository = repository;
    }



    public Function createFunction(@NotNull Function newFunction, @NotNull @Positive Long testId) {

        // TODO: See issue #1 at GitHub
        Test associatedTest = checkIfTestExists(testId);
        Function savedFunction = functionRepository.save(newFunction);

        associatedTest.setTargetFunction(savedFunction);
        testRepository.save(associatedTest);

        return savedFunction;
    }

    public void deleteFunction(@NotNull @Positive Long testId, @NotNull @Positive Long functionId) {

        checkIfTestExists(testId);

        Function existingFunction = checkIfFunctionExists(functionId);

        functionRepository.delete(existingFunction);
    }

    public void updateFunction(@NotNull @Positive Long testId, @NotNull @Positive Long functionId,
                                @NotNull FunctionModel model) {

        checkIfTestExists(testId);

        Function existingFunction = checkIfFunctionExists(functionId);

        setIfNotBlank(existingFunction::setName, model.getName());
        setIfNotBlank(existingFunction::setDescription, model.getDescription());
        setIfNotBlank(existingFunction::setGithubURL, model.getGithubURL());
        setIfNotBlank(existingFunction::setFunctionSourceDirectory, model.getFunctionSourceDirectory());
        setIfNotBlank(existingFunction::setFunctionSourceFileName, model.getFunctionSourceFileName());
        setIfNotBlank(existingFunction::setFunctionHandler, model.getFunctionHandler());

        functionRepository.save(existingFunction);
    }

    private void setIfNotBlank(Consumer<String> setter, String value) {

        if (StringUtils.isNotBlank(value)) {
            setter.accept(value);
        }
    }

    public Function getFunction(@NotNull @Positive Long testId, @NotNull @Positive Long functionId) {

        checkIfTestExists(testId);

        return checkIfFunctionExists(functionId);
    }


    private Test checkIfTestExists(Long associatedTestId) {

        return testRepository.findById(associatedTestId)
                .orElseThrow( () -> new TestNotFoundException(associatedTestId));
    }

    private Function checkIfFunctionExists(Long functionId) {

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();

        return functionRepository.findFunctionByIdAndAuthor(functionId, loggedInUser)
                .orElseThrow( () -> new FunctionNotFoundException(functionId));
    }


}
