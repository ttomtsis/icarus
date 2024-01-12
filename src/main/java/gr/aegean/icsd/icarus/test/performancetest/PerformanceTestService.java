package gr.aegean.icsd.icarus.test.performancetest;

import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.test.TestService;
import gr.aegean.icsd.icarus.testexecution.TestExecution;
import gr.aegean.icsd.icarus.testexecution.TestExecutionService;
import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import gr.aegean.icsd.icarus.util.enums.TestState;
import gr.aegean.icsd.icarus.util.exceptions.async.TestExecutionFailedException;
import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.entity.InvalidTestConfigurationException;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import gr.aegean.icsd.icarus.util.terraform.StackDeployer;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


@Service
@Transactional
@Validated
public class PerformanceTestService extends TestService {


    private final PerformanceTestRepository repository;
    private final TestExecutionService testExecutionService;


    private static final Logger log = LoggerFactory.getLogger("Performance Test Service");



    public PerformanceTestService(TestRepository testRepository, PerformanceTestRepository repository,
                                  StackDeployer deployer, TestExecutionService testExecutionService) {

        super(testRepository, deployer);
        this.repository = repository;
        this.testExecutionService = testExecutionService;
    }



    public PerformanceTest searchTest(@NotNull @Positive Long testId) {

        return checkIfPerformanceTestExists(testId);
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

        PerformanceTest requestedTest = checkIfPerformanceTestExists(testId);
        super.updateTest(requestedTest, testModel);

        super.setIfNotNull(requestedTest::setPathVariableValue, testModel.getPathVariableValue());
        super.setIfNotNull(requestedTest::setRequestBody, testModel.getRequestBody());

        if (testModel.getChosenMetrics() != null) {
            requestedTest.setChosenMetrics(testModel.getChosenMetrics());
        }

        repository.save(requestedTest);
    }


    public void executeTest(@NotNull @Positive Long testId, @NotBlank String deploymentId) {

        log.warn("Executing request: {}", deploymentId);

        PerformanceTest requestedTest = checkIfPerformanceTestExists(testId);
        super.executeTest(requestedTest);

        if (requestedTest.getLoadProfiles().isEmpty()) {
            throw new InvalidTestConfigurationException(testId, " does not have any Load Profiles" +
                    " associated with it");
        }

        if (requestedTest.getChosenMetrics().isEmpty()) {
            throw new InvalidTestConfigurationException(testId, " does not have any Metrics" +
                    " associated with it");
        }

        log.warn("All checks passed for: {}", deploymentId);

        TestExecution testExecution = testExecutionService.createEmptyExecution(requestedTest, deploymentId);
        testExecutionService.setExecutionState(testExecution, TestState.DEPLOYING);

        log.warn("Starting deployment of: {}", deploymentId);

        IcarusUser creator = UserUtils.getLoggedInUser();

        super.getDeployer().deploy(requestedTest, deploymentId)

            .exceptionally(ex -> {

                testExecutionService.abortTestExecution(testExecution, deploymentId);
                throw new TestExecutionFailedException(ex);
            })

            .thenAccept(result -> {

                testExecutionService.setExecutionState(testExecution, TestState.RUNNING);

                try {
                    
                    log.warn("Creating Load Tests: {}", deploymentId);
                    MetricQueryEngine queryEngine = new MetricQueryEngine(requestedTest, result, creator);

                    log.warn("Saving execution results: {}", deploymentId);

                    testExecutionService.produceReport(
                            testExecutionService.saveMetricResults(testExecution, queryEngine.getMetricResults())
                    );

                } catch (RuntimeException ex) {

                    log.error("Failed to execute tests: {}", deploymentId);

                    testExecutionService.abortTestExecution(testExecution, deploymentId);
                    throw new TestExecutionFailedException(ex);
                }
                
                log.warn("Test Completed, Deleting Stack: {}", deploymentId);
                testExecutionService.finalizeTestExecution(testExecution, deploymentId);

                log.warn("Finished: {}", deploymentId);
            });

    }



    private PerformanceTest checkIfPerformanceTestExists(Long testId) {

        return repository.findPerformanceTestByIdAndCreator(testId, UserUtils.getLoggedInUser())
                .orElseThrow(() -> new EntityNotFoundException(PerformanceTest.class, testId));
    }


}
