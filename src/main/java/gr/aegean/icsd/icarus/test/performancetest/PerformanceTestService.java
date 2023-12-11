package gr.aegean.icsd.icarus.test.performancetest;

import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.util.PatchDocument;
import gr.aegean.icsd.icarus.util.enums.Metric;
import gr.aegean.icsd.icarus.util.enums.PatchOperation;
import gr.aegean.icsd.icarus.util.exceptions.InvalidTestConfigurationException;
import gr.aegean.icsd.icarus.util.exceptions.TestNotFoundException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;


@Service
public class PerformanceTestService {


    private final TestRepository repository;



    public PerformanceTestService(TestRepository repository) {
        this.repository = repository;
    }



    public PerformanceTest searchTest(@NotNull @Positive Long testId) {

        return (PerformanceTest) repository.findById(testId)
                .orElseThrow(() -> new TestNotFoundException
                        ("Performance Test with id: " + testId + " was not found"));

    }

    public PerformanceTest createTest(@NotNull PerformanceTest newTest) {

        if (StringUtils.isBlank(newTest.getPath())) {

            if (!StringUtils.isBlank(newTest.getPathVariable())) {
                throw new InvalidTestConfigurationException
                        ("Cannot set a Path variable if the test does not expose a path");
            }
            if (!StringUtils.isBlank(newTest.getPathVariableValue())) {
                throw new InvalidTestConfigurationException
                        ("Cannot set a Path variable value if the test does not expose a path");
            }

        }

        if (newTest.getChosenMetrics().isEmpty()) {
            throw new InvalidTestConfigurationException
                    ("A Performance test must utilize at least 1 Metric");
        }

        return repository.save(newTest);

    }

    public void updateTest(@NotNull @Positive Long testId, @NotNull PerformanceTestModel test) {

        PerformanceTest requestedTest = (PerformanceTest) repository.findById(testId).orElseThrow( () ->
                new TestNotFoundException
                        ("Performance Test with ID: " + testId + " was not found"));

        setIfNotBlank(requestedTest::setName, test.getName());
        setIfNotBlank(value -> requestedTest.setHttpMethod(HttpMethod.valueOf(value)), test.getHttpMethod());
        setIfNotBlank(requestedTest::setDescription, test.getDescription());
        setIfNotBlank(requestedTest::setPathVariable, test.getPathVariable());
        setIfNotBlank(requestedTest::setPath, test.getPath());
        setIfNotBlank(requestedTest::setPathVariableValue, test.getPathVariableValue());
        setIfNotBlank(requestedTest::setRequestBody, test.getRequestBody());

        if (!test.getChosenMetrics().isEmpty()) {
            requestedTest.setChosenMetrics(test.getChosenMetrics());
        }

        LoggerFactory.getLogger("bobz").warn("path: " + test.getPath());
        repository.save(requestedTest);

    }

    private void setIfNotBlank(Consumer<String> setter, String value) {

        if (StringUtils.isNotBlank(value)) {
            setter.accept(value);
        }
    }

    public void updateTestMetrics(@NotNull @Positive Long testId, @NotNull PatchDocument patchDoc) {

        if (StringUtils.isBlank(patchDoc.getOp().toString()) ||
                (StringUtils.isBlank(patchDoc.getValue()) && !patchDoc.getOp().equals(PatchOperation.REMOVE))
        ) {

            throw new HttpMessageNotReadableException("Patch document is missing required fields");

        }

        Metric patchedMetric;
        try {
            patchedMetric = Metric.valueOf(patchDoc.getValue());
        }
        catch (IllegalArgumentException ex) {
            throw new HttpMessageNotReadableException(patchDoc.getValue() + " is not a valid Metric name", ex);
        }


        PerformanceTest requestedTest = (PerformanceTest) repository.findById(testId).orElseThrow( () ->
                new TestNotFoundException
                        ("Performance Test with ID: " + testId + " was not found"));


        if (patchDoc.getOp().equals(PatchOperation.ADD)) {
            requestedTest.addMetric(patchedMetric);
        }

        if (patchDoc.getOp().equals(PatchOperation.REMOVE)) {
            requestedTest.removeMetric(patchedMetric);
        }

    }

    public void deleteTest(@NotNull @Positive Long testId) {

        if (repository.existsById(testId)) {

            repository.deleteById(testId);

        } else {

            throw new TestNotFoundException("Performance Test with id " + testId + " does not exist");

        }

    }



}
