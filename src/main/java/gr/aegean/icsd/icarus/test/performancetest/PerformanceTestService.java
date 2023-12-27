package gr.aegean.icsd.icarus.test.performancetest;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.test.TestService;
import gr.aegean.icsd.icarus.testexecution.MetricResultRepository;
import gr.aegean.icsd.icarus.util.MetricQueryEngine;
import gr.aegean.icsd.icarus.util.enums.TestState;
import gr.aegean.icsd.icarus.util.exceptions.test.InvalidTestConfigurationException;
import gr.aegean.icsd.icarus.util.exceptions.test.TestExecutionFailedException;
import gr.aegean.icsd.icarus.util.terraform.StackDeployer;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;


@Service
@Transactional
@Validated
public class PerformanceTestService extends TestService {


    private final TestRepository repository;
    private final MetricResultRepository metricResultRepository;
    
    private static final Logger log = LoggerFactory.getLogger("Performance Test Service");



    public PerformanceTestService(TestRepository repository, StackDeployer deployer,
                                  MetricResultRepository metricResultRepository) {

        super(repository, deployer);
        this.repository = repository;
        this.metricResultRepository = metricResultRepository;
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

        super.setIfNotNull(requestedTest::setPathVariableValue, testModel.getPathVariableValue());
        super.setIfNotNull(requestedTest::setRequestBody, testModel.getRequestBody());

        if (testModel.getChosenMetrics() != null) {
            requestedTest.setChosenMetrics(testModel.getChosenMetrics());
        }

        repository.save(requestedTest);
    }

    @Override
    public Test executeTest(@NotNull @Positive Long testId) {

        PerformanceTest requestedTest = (PerformanceTest) super.executeTest(testId);

        if (requestedTest.getLoadProfiles().isEmpty()) {
            throw new InvalidTestConfigurationException(testId, " does not have any Load Profiles" +
                    " associated with it");
        }

        if (requestedTest.getChosenMetrics().isEmpty()) {
            throw new InvalidTestConfigurationException(testId, " does not have any Metrics" +
                    " associated with it");
        }

        super.setState(requestedTest, TestState.DEPLOYING);

        String deploymentId = UUID.randomUUID().toString().substring(0, 5);

        super.getDeployer().deploy(requestedTest, deploymentId)

            .exceptionally(ex -> {

                super.abortTestExecution(requestedTest, deploymentId);
                throw new TestExecutionFailedException(ex);
            })

            .thenAccept(result -> {

                super.setState(requestedTest, TestState.RUNNING);

                try {
                    
                    log.warn("Creating Load Tests");
                    MetricQueryEngine queryEngine = new MetricQueryEngine(requestedTest, result, deploymentId);

                    metricResultRepository.saveAll(queryEngine.getResultList());

                } catch (RuntimeException ex) {

                    super.abortTestExecution(requestedTest, deploymentId);
                    throw new TestExecutionFailedException(ex);
                }
                
                log.warn("Test Completed, Deleting Stack");

                super.finalizeTestExecution(requestedTest, deploymentId);

                log.warn("Finished");
            });

        return null;
    }


}
