package gr.aegean.icsd.icarus.test.performancetest;

import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.test.TestService;
import gr.aegean.icsd.icarus.util.exceptions.InvalidTestConfigurationException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Service
public class PerformanceTestService extends TestService {


    private final TestRepository repository;



    public PerformanceTestService(TestRepository repository) {
        super(repository);
        this.repository = repository;
    }



    @Override
    public PerformanceTest searchTest(@NotNull @Positive Long testId) {

        return (PerformanceTest) super.searchTest(testId);
    }

    public PerformanceTest createTest(@NotNull PerformanceTest newTest) {

        if (!StringUtils.isBlank(newTest.getPathVariableValue()) &&
                StringUtils.isBlank(newTest.getPath())) {

            throw new InvalidTestConfigurationException
                    ("Cannot set a Path variable value if the test does not expose a path");
        }

        if (newTest.getChosenMetrics() == null) {
            throw new InvalidTestConfigurationException
                    ("A Performance test must utilize at least 1 Metric");
        }

        return (PerformanceTest) super.createTest(newTest);
    }

    public void updateTest(@NotNull @Positive Long testId, @NotNull PerformanceTestModel testModel) {

        PerformanceTest requestedTest = (PerformanceTest) super.updateTest(testId, testModel);

        super.setIfNotBlank(requestedTest::setPathVariableValue, testModel.getPathVariableValue());
        super.setIfNotBlank(requestedTest::setRequestBody, testModel.getRequestBody());

        if (testModel.getChosenMetrics() != null) {
            requestedTest.setChosenMetrics(testModel.getChosenMetrics());
        }

        repository.save(requestedTest);
    }


}
