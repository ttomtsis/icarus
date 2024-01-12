package gr.aegean.icsd.icarus.test;

import gr.aegean.icsd.icarus.function.Function;
import gr.aegean.icsd.icarus.provideraccount.ProviderAccount;
import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import gr.aegean.icsd.icarus.util.enums.Platform;
import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.entity.InvalidTestConfigurationException;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import gr.aegean.icsd.icarus.util.terraform.StackDeployer;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;


@Service
@Transactional
@Validated
public class TestService {


    private final TestRepository repository;

    private final StackDeployer deployer;


    public TestService(TestRepository repository,
                       StackDeployer deployer) {

        this.repository = repository;
        this.deployer = deployer;
    }



    public Test createTest(@NotNull Test newTest) {

        if (StringUtils.isBlank(newTest.getPath()) &&
                !StringUtils.isBlank(newTest.getPathVariable())) {

            throw new InvalidTestConfigurationException
                    ("Cannot set a Path variable if the test does not expose a path");
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
            throw new InvalidTestConfigurationException(requestedTest.getId(), "does not have a Function" +
                    " associated with it");
        }

        // Test has at least 1 provider account
        if (requestedTest.getAccountsList().isEmpty()) {
            throw new InvalidTestConfigurationException(requestedTest.getId(), "does not have " +
                    "any provider accounts associated with it");
        }

        // Test has one configuration per provider account
        if (!oneConfigurationPerProviderAccount(requestedTest)) {
            throw new InvalidTestConfigurationException(requestedTest.getId(), "does not have a resource" +
                    " configuration for every provider account");
        }

        String functionSourceCodeLocation = requestedTest.getTargetFunction().getFunctionSourceDirectory()
                + "\\" + requestedTest.getTargetFunction().getFunctionSourceFileName();

        if(!Files.exists(Paths.get(functionSourceCodeLocation))) {
            throw new InvalidTestConfigurationException(requestedTest.getId(), " is unable to find the source code" +
                    " of it's target Function in the filesystem");
        }

    }


    private boolean oneConfigurationPerProviderAccount(Test requestedTest) {

        for (ProviderAccount account : requestedTest.getAccountsList()) {

            boolean foundAssociatedConfiguration = false;
            for (ResourceConfiguration configuration : requestedTest.getResourceConfigurations()){

                if (account.getAccountType().equals("AwsAccount") &&
                        configuration.getProviderPlatform().equals(Platform.AWS)) {

                    foundAssociatedConfiguration = true;
                    break;
                }

                if (account.getAccountType().equals("GcpAccount") &&
                        configuration.getProviderPlatform().equals(Platform.GCP)) {

                    foundAssociatedConfiguration = true;
                    break;
                }

            }

            if (!foundAssociatedConfiguration) {
                return false;
            }
        }

        return true;
    }


    protected void setIfNotNull(Consumer<String> setter, String value) {

        if (value != null) {
            setter.accept(value);
        }
    }

    protected StackDeployer getDeployer() {
        return this.deployer;
    }

    private Test checkIfTestExists(Long testId) {

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();

        return repository.findTestByIdAndCreator(testId, loggedInUser)
                .orElseThrow(() -> new EntityNotFoundException
                        (Test.class, testId));
    }

}
