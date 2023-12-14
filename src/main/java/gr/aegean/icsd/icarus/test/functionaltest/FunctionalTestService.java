package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.provideraccount.ProviderAccount;
import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.test.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.util.enums.Platform;
import gr.aegean.icsd.icarus.util.enums.TestState;
import gr.aegean.icsd.icarus.util.exceptions.InvalidTestConfigurationException;
import gr.aegean.icsd.icarus.util.exceptions.InvalidTestStateException;
import gr.aegean.icsd.icarus.util.exceptions.TestNotFoundException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;


@Service
public class FunctionalTestService {


    private final TestRepository repository;


    public FunctionalTestService(TestRepository repository) {
        this.repository = repository;
    }



    public FunctionalTest searchTest(@NotNull @Positive Long testId) {

        return (FunctionalTest) repository.findById(testId)
                .orElseThrow(() -> new TestNotFoundException
                        (testId));

    }

    public FunctionalTest createTest(@NotNull FunctionalTest newTest) {

        return repository.save(newTest);

    }

    public void updateTest(@NotNull @Positive Long testId, @NotNull FunctionalTestModel test) {

        FunctionalTest requestedTest = (FunctionalTest) repository.findById(testId).orElseThrow( () ->
                new TestNotFoundException
                        (testId));

        setIfNotBlank(requestedTest::setName, test.getName());
        setIfNotBlank(value -> requestedTest.setHttpMethod(HttpMethod.valueOf(value)), test.getHttpMethod());
        setIfNotBlank(requestedTest::setDescription, test.getDescription());
        setIfNotBlank(requestedTest::setPathVariable, test.getPathVariable());
        setIfNotBlank(requestedTest::setPath, test.getPath());

        setIfNotBlank(requestedTest::setRegion, test.getRegion());
        setIfNotBlank(requestedTest::setFunctionURL, test.getFunctionUrl());

        if (test.getUsedMemory() != null && test.getUsedMemory() >= 0) {
            requestedTest.setUsedMemory(test.getUsedMemory());
        }

        repository.save(requestedTest);

    }

    private void setIfNotBlank(Consumer<String> setter, String value) {

        if (StringUtils.isNotBlank(value)) {
            setter.accept(value);
        }
    }

    public void deleteTest(@NotNull @Positive Long testId) {

        if (repository.existsById(testId)) {

            repository.deleteById(testId);

        } else {

            throw new TestNotFoundException(testId);

        }

    }

    public void executeTest(@NotNull @Positive Long testId) {

        // Test exists
        FunctionalTest requestedTest = (FunctionalTest) repository.findById(testId)
                .orElseThrow(() -> new TestNotFoundException
                        (testId));

        // Test is in correct state
        if (!requestedTest.getState().equals(TestState.CREATED)) {
            throw new InvalidTestStateException(testId, requestedTest.getState(), TestState.CREATED);
        }

        // Test has a Function associated with it
        if (requestedTest.getTargetFunction() == null) {
            throw new InvalidTestConfigurationException("Test with id: " + testId + " does not have a Function" +
                    "associated with it");
        }

        // Test has at least 1 provider account
        if (requestedTest.getAccountsList().isEmpty()) {
            throw new InvalidTestConfigurationException("Test with id: " + testId + " does not have " +
                    "any provider accounts associated with it");
        }

        // Test has one configuration per provider account
        if (!oneConfigurationPerProviderAccount(requestedTest)) {
            throw new InvalidTestConfigurationException("Test with id: " + testId + " does not have a resource" +
                    "configuration for every provider account");
        }


    }

    public boolean oneConfigurationPerProviderAccount(FunctionalTest requestedTest) {

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


}
