package gr.aegean.icsd.icarus.test.performancetest;

import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import gr.aegean.icsd.icarus.provideraccount.AwsAccount;
import gr.aegean.icsd.icarus.provideraccount.ProviderAccount;
import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.test.TestService;
import gr.aegean.icsd.icarus.testexecution.TestExecution;
import gr.aegean.icsd.icarus.testexecution.TestExecutionService;
import gr.aegean.icsd.icarus.util.enums.ExecutionState;
import gr.aegean.icsd.icarus.util.enums.Platform;
import gr.aegean.icsd.icarus.util.exceptions.async.AsyncExecutionFailedException;
import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.entity.InvalidEntityConfigurationException;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import gr.aegean.icsd.icarus.util.terraform.DeploymentRecord;
import gr.aegean.icsd.icarus.util.terraform.FunctionDeployer;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import java.util.Set;


@Service
@Transactional
@Validated
public class PerformanceTestService extends TestService {


    private final PerformanceTestRepository repository;
    private final TestExecutionService testExecutionService;


    private static final Logger log = LoggerFactory.getLogger("Performance Test Service");



    public PerformanceTestService(TestRepository testRepository, PerformanceTestRepository repository,
                                  FunctionDeployer deployer, TestExecutionService testExecutionService) {

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

            throw new InvalidEntityConfigurationException
                    (PerformanceTest.class, "Cannot set a Path variable value if the test does not expose a path");
        }

        if (newTest.getChosenMetrics() == null) {
            throw new InvalidEntityConfigurationException
                    (PerformanceTest.class, "A Performance test must utilize at least 1 Metric");
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


    @Async
    public void executeTest(@NotNull PerformanceTest requestedTest, @NotBlank String deploymentId,
                            @NotNull IcarusUser creator) {

        log.warn("All checks passed for: {}", deploymentId);

        TestExecution testExecution = testExecutionService.createEmptyExecution(requestedTest, deploymentId, creator);
        testExecutionService.setExecutionState(testExecution, ExecutionState.DEPLOYING);

        log.warn("Starting deployment of: {} for Execution: {}", deploymentId, testExecution.getId());

        Set<DeploymentRecord> deploymentRecords = deployFunctions(requestedTest, testExecution, deploymentId);
        executePerformanceTest(requestedTest, deploymentId, testExecution, creator, deploymentRecords);
    }


    private Set<DeploymentRecord> deployFunctions(PerformanceTest requestedTest, TestExecution testExecution, String deploymentId) {
        try{
            testExecutionService.setExecutionState(testExecution, ExecutionState.RUNNING);
            return super.getDeployer()
                    .deployFunctions(requestedTest, requestedTest.getResourceConfigurations(), deploymentId);
        }
        catch (RuntimeException ex) {
            testExecutionService.abortTestExecution(testExecution, deploymentId);
            throw new AsyncExecutionFailedException(ex);
        }
    }


    private void executePerformanceTest(PerformanceTest requestedTest, String deploymentId,
                                        TestExecution testExecution, IcarusUser creator,
                                        Set<DeploymentRecord> deploymentRecords) {

            try {

                log.warn("Creating Load Tests: {} for Execution {}", deploymentId, testExecution.getId());
                MetricQueryEngine queryEngine = new MetricQueryEngine(requestedTest, deploymentRecords, creator);

                log.warn("Saving execution results: {} for Execution {}", deploymentId, testExecution.getId());
                testExecutionService.saveMetricResults(testExecution, queryEngine.getMetricResults());

                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        testExecutionService.produceReport(testExecution);
                        log.warn("Finished: {} - Execution: {}", deploymentId, testExecution.getId());
                    }
                });

            } catch (RuntimeException ex) {

                log.error("Failed to execute tests: {}", deploymentId);

                testExecutionService.abortTestExecution(testExecution, deploymentId);
                throw new AsyncExecutionFailedException(ex);
            }

            log.warn("Test Completed, Deleting Stack: {} for Execution: {}", deploymentId, testExecution.getId());
            testExecutionService.finalizeTestExecution(testExecution, deploymentId);
    }



    private PerformanceTest checkIfPerformanceTestExists(Long testId) {

        return repository.findPerformanceTestByIdAndCreator(testId, UserUtils.getLoggedInUser())
                .orElseThrow(() -> new EntityNotFoundException(PerformanceTest.class, testId));
    }


    public PerformanceTest validateTest(@NotNull @Positive Long testId) {

        PerformanceTest requestedTest = checkIfPerformanceTestExists(testId);

        super.executeTest(requestedTest);

        if (requestedTest.getLoadProfiles().isEmpty()) {
            throw new InvalidEntityConfigurationException(PerformanceTest.class, requestedTest.getId(),
                    " does not have any Load Profiles" +
                            " associated with it");
        }

        if (requestedTest.getChosenMetrics().isEmpty()) {
            throw new InvalidEntityConfigurationException(PerformanceTest.class, requestedTest.getId(),
                    " does not have any Metrics" +
                            " associated with it");
        }

        for (ProviderAccount account : requestedTest.getAccountsList()) {

            boolean foundAssociatedConfiguration = false;

                for (ResourceConfiguration configuration : requestedTest.getResourceConfigurations()){

                    if (account instanceof AwsAccount &&
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
                throw new InvalidEntityConfigurationException(PerformanceTest.class, requestedTest.getId(),
                        "does not have a resource" +
                                " configuration for every provider account");
            }
        }

        return requestedTest;
    }


}
