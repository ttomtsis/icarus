package gr.aegean.icsd.icarus.test.performancetest;

import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.util.exceptions.InvalidTestConfigurationException;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Service
public class PerformanceTestService {


    private final TestRepository repository;



    public PerformanceTestService(TestRepository repository) {
        this.repository = repository;
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


}
