package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.function.Function;
import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import gr.aegean.icsd.icarus.provideraccount.AwsAccount;
import gr.aegean.icsd.icarus.provideraccount.GcpAccount;
import gr.aegean.icsd.icarus.provideraccount.ProviderAccount;
import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.test.TestService;
import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCase;
import gr.aegean.icsd.icarus.test.functionaltest.testcasemember.TestCaseMember;
import gr.aegean.icsd.icarus.testexecution.TestExecution;
import gr.aegean.icsd.icarus.testexecution.TestExecutionService;
import gr.aegean.icsd.icarus.testexecution.testcaseresult.TestCaseResult;
import gr.aegean.icsd.icarus.testexecution.testcaseresult.TestCaseResultRepository;
import gr.aegean.icsd.icarus.util.enums.ExecutionState;
import gr.aegean.icsd.icarus.util.enums.Platform;
import gr.aegean.icsd.icarus.util.exceptions.async.AsyncExecutionFailedException;
import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.entity.InvalidEntityConfigurationException;
import gr.aegean.icsd.icarus.util.restassured.RestAssuredTest;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import gr.aegean.icsd.icarus.util.terraform.DeploymentRecord;
import gr.aegean.icsd.icarus.util.terraform.FunctionDeployer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.Set;


@Service
@Transactional
@Validated
public class FunctionalTestService extends TestService {


    private final FunctionalTestRepository repository;
    private final TestExecutionService testExecutionService;

    private static final Logger log = LoggerFactory.getLogger("Functional Test Service");



    public FunctionalTestService(FunctionalTestRepository repository, TestRepository testRepository,
                                 FunctionDeployer deployer, TestExecutionService testExecutionService) {

        super(testRepository, deployer);
        this.repository = repository;
        this.testExecutionService = testExecutionService;
    }



    public FunctionalTest createTest(@NotNull FunctionalTest newTest) {

        return (FunctionalTest) super.createTest(newTest);
    }


    public FunctionalTest searchTest(@NotNull @Positive Long testId) {

        return checkIfFunctionalTestExists(testId);
    }


    public void updateTest(@NotNull @Positive Long testId, @NotNull FunctionalTestModel testModel) {

        FunctionalTest requestedTest = checkIfFunctionalTestExists(testId);
        super.updateTest(requestedTest, testModel);

        super.setIfNotNull(requestedTest::setFunctionURL, testModel.getFunctionUrl());

        repository.save(requestedTest);
    }



    public void executeTest(@NotNull @Positive Long testId, @NotBlank String deploymentId) {

        log.warn("Executing request: {}", deploymentId);

        FunctionalTest requestedTest = checkIfFunctionalTestExists(testId);
        validateTest(requestedTest);

        log.warn("All checks passed for: {}, creating Test Execution", deploymentId);

        TestExecution testExecution = testExecutionService.createEmptyExecution(requestedTest, deploymentId);
        testExecutionService.setExecutionState(testExecution, ExecutionState.DEPLOYING);

        IcarusUser creator = UserUtils.getLoggedInUser();

        if (StringUtils.isBlank(requestedTest.getFunctionURL())) {

            log.warn("Starting deployment of: {} for Execution {}", deploymentId, testExecution.getId());
            deployFunctionAndExecuteTest(requestedTest, deploymentId, testExecution, creator);
        }
        else {

            log.warn("Function is already deployed, will execute Functional Test for: {} and Execution: {}",
                    deploymentId, testExecution.getId());

            Set<DeploymentRecord> deploymentRecords = createDeploymentRecord(requestedTest, deploymentId);
            executeFunctionalTest(requestedTest, testExecution, deploymentRecords, deploymentId, creator);
        }


    }



    private void deployFunctionAndExecuteTest(FunctionalTest requestedTest, String deploymentId,
                                              TestExecution testExecution, IcarusUser creator) {

        Set<ResourceConfiguration> configurations = new HashSet<>();
        configurations.add(requestedTest.getResourceConfiguration());

        super.getDeployer().deployFunctions(requestedTest, configurations, deploymentId)

                .exceptionally(ex -> {

                    log.error("Deployment of: {} and Execution {} - FAILED", deploymentId, testExecution.getId());
                    testExecutionService.abortTestExecution(testExecution, deploymentId);
                    throw new AsyncExecutionFailedException(ex);
                })

                .thenAccept(deploymentRecords ->
                        executeFunctionalTest(requestedTest, testExecution, deploymentRecords, deploymentId, creator));

    }



    @Autowired
    TestCaseResultRepository testCaseResultRepository;
    @Transactional(propagation = Propagation.MANDATORY)
    public void executeFunctionalTest(FunctionalTest requestedTest, TestExecution testExecution,
                                       Set<DeploymentRecord> deploymentRecords, String deploymentId,
                                       IcarusUser creator) {

        testExecutionService.setExecutionState(testExecution, ExecutionState.RUNNING);

        try {

            log.warn("Creating Rest Assured Tests of: {} for Execution: {}",
                    deploymentId, testExecution.getId());
            Set<TestCaseResult> results = createRestAssuredTests(requestedTest, deploymentRecords, creator);

            log.warn("Saving execution results of: {} for Execution: {}",
                    deploymentId, testExecution.getId());


            testExecutionService.saveTestCaseResults(testExecution, results);
            if(TransactionSynchronizationManager.isActualTransactionActive()) {
                LoggerFactory.getLogger("bobz").warn("TRANSACTION EXISTS");
            }
            else {
                LoggerFactory.getLogger("bobz").warn("NO TRANSACTION LUL");
            }
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronizationAdapter() {
                        @Override
                        public void afterCommit() {

                            for (TestCaseResult result : testExecution.getTestCaseResults()) {
                                TestCaseResult dbResult = testCaseResultRepository.findById(result.getId())
                                        .orElseThrow(() -> new RuntimeException("poop! no " + result.getId() + " exists"));
                                LoggerFactory.getLogger("bobz").warn("found result: {}", dbResult.getId());
                            }

                            testExecutionService.produceReport(testExecution);

                            LoggerFactory.getLogger("bobz").warn("done");
                        }
                    }
            );

        } catch (RuntimeException ex) {

            log.error("Failed to execute tests: {}", deploymentId);

            testExecutionService.setExecutionState(testExecution, ExecutionState.ERROR);
            throw new AsyncExecutionFailedException(ex);
        }

        log.warn("Test completed: {} for Execution: {}",
                deploymentId, testExecution.getId());

        testExecutionService.setExecutionState(testExecution, ExecutionState.FINISHED);

        log.warn("Finished: {} - Execution: {}",
                deploymentId, testExecution.getId());

    }


    @Transactional(propagation = Propagation.MANDATORY)
    public Set<TestCaseResult> createRestAssuredTests(FunctionalTest requestedTest,
                                                       Set<DeploymentRecord> deploymentRecords,
                                                       IcarusUser creator) {

        Set<TestCaseResult> testCaseResults = new HashSet<>();
        Set<Thread> threadSet = new HashSet<>();

        for (DeploymentRecord deploymentRecord : deploymentRecords) {
            for (TestCase testCase : requestedTest.getTestCases()) {
                for (TestCaseMember testCaseMember : testCase.getTestCaseMembers()) {

                    Thread thread = new Thread(() -> {

                        log.warn("Running Test Case Member: {} for Function: {}",  testCaseMember.getId(),
                                deploymentRecord.deployedFunctionName);

                        // Create rest assured test
                        RestAssuredTest test = new RestAssuredTest(deploymentRecord.deployedUrl, requestedTest.getPath(),
                                requestedTest.getPathVariable(), testCaseMember.getRequestPathVariableValue(),
                                testCaseMember.getRequestBody(), testCaseMember.getExpectedResponseCode(),
                                testCaseMember.getExpectedResponseBody(),
                                deploymentRecord.deployedPlatform
                        );

                        log.warn("Saving Test Case Results of: {} for Function: {}", testCaseMember.getId(),
                                deploymentRecord.deployedFunctionName);

                        // Save results
                        TestCaseResult testResult = new TestCaseResult(testCaseMember,
                                deploymentRecord.configurationUsed,
                                test.getActualResponseCode(), test.getActualResponseBody(), test.getPass(),
                                creator
                        );

                        testCaseResults.add(testCaseResultRepository.saveAndFlush(testResult));
                    });

                    threadSet.add(thread);
                    thread.start();
                }
            }
        }

        for (Thread test : threadSet) {
            try {
                test.join();
            } catch (InterruptedException e) {
                throw new AsyncExecutionFailedException(e);
            }
        }

    return testCaseResults;
    }



    private FunctionalTest checkIfFunctionalTestExists(Long testId) {

        return repository.findFunctionalTestByIdAndCreator(testId, UserUtils.getLoggedInUser())
                .orElseThrow(() -> new EntityNotFoundException(FunctionalTest.class, testId));
    }

    private void validateTest(FunctionalTest requestedTest) {

        super.executeTest(requestedTest);

        // Has at least 1 TestCase
        if (requestedTest.getTestCases().isEmpty()) {
            throw new InvalidEntityConfigurationException(FunctionalTest.class, requestedTest.getId(),
                    "does not have any Test Cases" +
                    " associated with it");
        }

        // Has at least 1 Test Case Member
        boolean atLeastOneTestCaseMember = false;
        for (TestCase testCase : requestedTest.getTestCases()) {

            if (!testCase.getTestCaseMembers().isEmpty()) {

                atLeastOneTestCaseMember = true;
                break;
            }
        }

        if (!atLeastOneTestCaseMember) {
            throw new InvalidEntityConfigurationException(FunctionalTest.class, requestedTest.getId(),
                    "does not have any Test Case Members " +
                            "associated with it");
        }

        // One Resource configuration per account type
        for (ProviderAccount account : requestedTest.getAccountsList()) {

            boolean foundAssociatedConfiguration = false;

            if (account instanceof AwsAccount &&
                    requestedTest.getResourceConfiguration().getProviderPlatform().equals(Platform.AWS)) {
                foundAssociatedConfiguration = true;
            }

            if (account instanceof GcpAccount &&
                    requestedTest.getResourceConfiguration().getProviderPlatform().equals(Platform.GCP)) {
                foundAssociatedConfiguration = true;
            }

            if (!foundAssociatedConfiguration) {
                throw new InvalidEntityConfigurationException(FunctionalTest.class, requestedTest.getId(),
                        "does not have a resource" +
                                " configuration for every provider account");
            }

        }

    }

    private Set<DeploymentRecord> createDeploymentRecord(FunctionalTest requestedTest, String deploymentId) {

        Set<DeploymentRecord> deploymentRecords = new HashSet<>();

        ResourceConfiguration configuration = requestedTest.getResourceConfiguration();
        Function targetFunction = requestedTest.getTargetFunction();

        String functionName = targetFunction.getName() + "-" + deploymentId;

        Integer memory = configuration.getMemoryConfigurations().stream().findFirst()
                .orElseThrow(() -> new AsyncExecutionFailedException("The configuration associated with "
                        + deploymentId + " does not contain any memory specifications"));

        String region = configuration.getRegions().stream().findFirst()
                .orElseThrow(() -> new AsyncExecutionFailedException("The configuration associated with "
                        + deploymentId + " does not contain any region specifications"));


        Platform platform = configuration.getProviderPlatform();

        DeploymentRecord deploymentRecord = new DeploymentRecord(functionName, region, memory, deploymentId, platform);
        deploymentRecord.deployedUrl = requestedTest.getFunctionURL();
        deploymentRecord.configurationUsed = configuration;

        deploymentRecords.add(deploymentRecord);

        return deploymentRecords;
    }


}
