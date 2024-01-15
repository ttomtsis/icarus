package gr.aegean.icsd.icarus.test.functionaltest;

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
import gr.aegean.icsd.icarus.util.enums.ExecutionState;
import gr.aegean.icsd.icarus.util.enums.Platform;
import gr.aegean.icsd.icarus.util.exceptions.async.AsyncExecutionFailedException;
import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.entity.InvalidEntityConfigurationException;
import gr.aegean.icsd.icarus.util.restassured.RestAssuredTest;
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
import org.springframework.stereotype.Service;
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

        log.warn("All checks passed for: {}", deploymentId);

        TestExecution testExecution = testExecutionService.createEmptyExecution(requestedTest, deploymentId);
        testExecutionService.setExecutionState(testExecution, ExecutionState.DEPLOYING);

        IcarusUser creator = UserUtils.getLoggedInUser();

        if (StringUtils.isBlank(requestedTest.getFunctionURL())) {

            log.warn("Starting deployment of: {}", deploymentId);
            deployFunctionAndExecuteTest(requestedTest, deploymentId, testExecution, creator);
        }
        else {

            log.warn("Function is already deployed, will execute Functional Test for: {}", deploymentId);
            Set<DeploymentRecord> deploymentRecords = createDeploymentRecord(requestedTest);
            executeFunctionalTest(requestedTest, testExecution, deploymentRecords, deploymentId, creator);
        }


    }



    private void deployFunctionAndExecuteTest(FunctionalTest requestedTest, String deploymentId,
                                              TestExecution testExecution, IcarusUser creator) {

        Set<ResourceConfiguration> configurations = new HashSet<>();
        configurations.add(requestedTest.getResourceConfiguration());

        super.getDeployer().deployFunctions(requestedTest, configurations, deploymentId)

                .exceptionally(ex -> {

                    log.error("Deployment of: {} FAILED", deploymentId);
                    testExecutionService.abortTestExecution(testExecution, deploymentId);
                    throw new AsyncExecutionFailedException(ex);
                })

                .thenAccept(deploymentRecords ->
                        executeFunctionalTest(requestedTest, testExecution, deploymentRecords, deploymentId, creator));

    }


    private void executeFunctionalTest(FunctionalTest requestedTest, TestExecution testExecution,
                                       Set<DeploymentRecord> deploymentRecords, String deploymentId,
                                       IcarusUser creator) {

        testExecutionService.setExecutionState(testExecution, ExecutionState.RUNNING);

        try {

            log.warn("Creating Rest Assured Tests of: {}", deploymentId);
            Set<TestCaseResult> results = createRestAssuredTests(requestedTest, deploymentRecords, creator);

            log.warn("Saving execution results of: {}", deploymentId);

            testExecutionService.produceReport(
                    testExecutionService.saveTestCaseResults(testExecution, results)
            );

        } catch (RuntimeException ex) {

            log.error("Failed to execute tests: {}", deploymentId);

            testExecutionService.abortTestExecution(testExecution, deploymentId);
            throw new AsyncExecutionFailedException(ex);
        }

        log.warn("Test completed, Deleting stack: {}", deploymentId);
        testExecutionService.finalizeTestExecution(testExecution, deploymentId);

        log.warn("Finished: {}", deploymentId);
    }


    private Set<TestCaseResult> createRestAssuredTests(FunctionalTest requestedTest,
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

                        testCaseResults.add(testResult);
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
                    "associated with it");
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

    private Set<DeploymentRecord> createDeploymentRecord(FunctionalTest requestedTest) {

        return null;
    }


}
