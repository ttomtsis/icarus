package gr.aegean.icsd.icarus.test;

import gr.aegean.icsd.icarus.function.Function;
import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import gr.aegean.icsd.icarus.provideraccount.ProviderAccount;
import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.entity.InvalidEntityConfigurationException;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import gr.aegean.icsd.icarus.util.terraform.FunctionDeployer;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;


@Service
@Transactional
@Validated
public class TestService {


    private final TestRepository repository;

    private final FunctionDeployer deployer;


    public TestService(TestRepository repository,
                       FunctionDeployer deployer) {

        this.repository = repository;
        this.deployer = deployer;
    }



    public Test createTest(@NotNull Test newTest) {

        if (StringUtils.isBlank(newTest.getPath()) &&
                !StringUtils.isBlank(newTest.getPathVariable())) {

            throw new InvalidEntityConfigurationException
                    (Test.class, "Cannot set a Path variable if the test does not expose a path");
        }

        return repository.save(newTest);
    }

    public void updateTest(@NotNull Test requestedTest, @NotNull TestModel testModel) {

        setIfNotNull(requestedTest::setName, testModel.getName());
        setIfNotNull(requestedTest::setDescription, testModel.getDescription());

        if (testModel.getHttpMethod() != null) {
            requestedTest.setHttpMethod(testModel.getHttpMethod());
        }

        setIfNotNull(requestedTest::setPath, testModel.getPath());
        setIfNotNull(requestedTest::setPathVariable, testModel.getPathVariable());

        if (testModel.getTargetFunction() != null) {

            Function newFunction = new Function();
            newFunction.setId(testModel.getTargetFunction());

            requestedTest.setTargetFunction(newFunction);
        }

        if (testModel.getAccountsList() != null) {

            requestedTest.clearAccountsList();
            for (Long accountId : testModel.getAccountsList()) {

                ProviderAccount account = new ProviderAccount();
                account.setId(accountId);

                requestedTest.addProviderAccount(account);
            }
        }

    }

    public void deleteTest(@NotNull @Positive Long testId) {

        Test requestedTest = checkIfTestExists(testId);
        repository.delete(requestedTest);
    }

    public void executeTest(@NotNull Test requestedTest) {

        // Test has a Function associated with it
        if (requestedTest.getTargetFunction() == null) {
            throw new InvalidEntityConfigurationException(Test.class, requestedTest.getId(), "does not have a Function" +
                    " associated with it");
        }

        // Test has at least 1 provider account
        if (requestedTest.getAccountsList().isEmpty()) {
            throw new InvalidEntityConfigurationException(Test.class, requestedTest.getId(), "does not have " +
                    "any provider accounts associated with it");
        }

        // Function's source code is available
        String functionSourceCodeLocation = requestedTest.getTargetFunction().getFunctionSourceDirectory()
                + File.separator + requestedTest.getTargetFunction().getFunctionSourceFileName();

        if(!Files.exists(Paths.get(functionSourceCodeLocation))) {
            throw new InvalidEntityConfigurationException(Test.class, requestedTest.getId(),
                    " is unable to find the source code" +
                    " of it's target Function in the filesystem");
        }

    }



    protected void setIfNotNull(Consumer<String> setter, String value) {

        if (value != null) {
            setter.accept(value);
        }
    }

    protected FunctionDeployer getDeployer() {
        return this.deployer;
    }

    private Test checkIfTestExists(Long testId) {

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();

        return repository.findTestByIdAndCreator(testId, loggedInUser)
                .orElseThrow(() -> new EntityNotFoundException
                        (Test.class, testId));
    }

}
