package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.util.enums.TestState;
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

        FunctionalTest requestedTest = (FunctionalTest) repository.findById(testId)
                .orElseThrow(() -> new TestNotFoundException
                        (testId));

        if (!requestedTest.getState().equals(TestState.CREATED)) {
            throw new InvalidTestStateException(testId, requestedTest.getState(), TestState.CREATED);
        }



    }


}
