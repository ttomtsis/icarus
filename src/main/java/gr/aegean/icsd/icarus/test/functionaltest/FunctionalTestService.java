package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.test.TestService;
import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCase;
import gr.aegean.icsd.icarus.test.functionaltest.testcasemember.TestCaseMember;
import gr.aegean.icsd.icarus.testexecution.TestCaseResult;
import gr.aegean.icsd.icarus.testexecution.TestCaseResultRepository;
import gr.aegean.icsd.icarus.util.enums.TestState;
import gr.aegean.icsd.icarus.util.exceptions.test.InvalidTestConfigurationException;
import gr.aegean.icsd.icarus.util.exceptions.test.TestExecutionFailedException;
import gr.aegean.icsd.icarus.util.restassured.RestAssuredTest;
import gr.aegean.icsd.icarus.util.terraform.DeploymentRecord;
import gr.aegean.icsd.icarus.util.terraform.StackDeployer;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Set;
import java.util.UUID;


@Service
@Transactional
@Validated
public class FunctionalTestService extends TestService {


    private final TestRepository testRepository;
    private final TestCaseResultRepository testCaseResultRepository;



    public FunctionalTestService(TestRepository repository, StackDeployer deployer,
                                 TestCaseResultRepository testCaseResultRepository) {
        super(repository, deployer);
        this.testRepository = repository;
        this.testCaseResultRepository = testCaseResultRepository;
    }



    public FunctionalTest createTest(@NotNull FunctionalTest newTest) {

        return (FunctionalTest) super.createTest(newTest);
    }

    @Override
    public FunctionalTest searchTest(@NotNull @Positive Long testId) {

        return (FunctionalTest) super.searchTest(testId);
    }

    public void updateTest(@NotNull @Positive Long testId, @NotNull FunctionalTestModel testModel) {

        FunctionalTest requestedTest = (FunctionalTest) super.updateTest(testId, testModel);

        super.setIfNotNull(requestedTest::setFunctionURL, testModel.getFunctionUrl());

        testRepository.save(requestedTest);
    }

    @Override
    public Test executeTest(@NotNull @Positive Long testId) {

        FunctionalTest requestedTest = (FunctionalTest) super.executeTest(testId);

        // Has at least 1 TestCase
        if (requestedTest.getTestCases().isEmpty()) {
            throw new InvalidTestConfigurationException(testId, "does not have any Test Cases" +
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
            throw new InvalidTestConfigurationException(testId, "does not have any Test Case Members " +
                    "associated with it");
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

                    createRestAssuredTests(requestedTest, result);

                } catch (RuntimeException ex) {

                    super.abortTestExecution(requestedTest, deploymentId);
                    throw new TestExecutionFailedException(ex);
                }

                super.finalizeTestExecution(requestedTest, deploymentId);
            });

        return null;
    }


    private void createRestAssuredTests(FunctionalTest requestedTest, Set<DeploymentRecord> deploymentRecords) {

        for (DeploymentRecord deploymentRecord : deploymentRecords) {
            for (TestCase testCase : requestedTest.getTestCases()) {
                for (TestCaseMember testCaseMember : testCase.getTestCaseMembers()) {

                    LoggerFactory.getLogger("Functional Test Service").warn("Running test");

                    // Create rest assured test
                    RestAssuredTest test = new RestAssuredTest(deploymentRecord.deployedUrl, requestedTest.getPath(),
                            requestedTest.getPathVariable(), testCaseMember.getRequestPathVariableValue(),
                            testCaseMember.getRequestBody(), testCaseMember.getExpectedResponseCode(),
                            testCaseMember.getExpectedResponseBody(),
                            deploymentRecord.deployedPlatform
                    );

                    LoggerFactory.getLogger("Functional Test Service").warn("Saving results");

                    // Save results
                    TestCaseResult testResult = new TestCaseResult(testCaseMember,
                            deploymentRecord.configurationUsed,
                            test.getActualResponseCode(), test.getActualResponseBody(), test.getPass());

                    testCaseResultRepository.save(testResult);
                }
            }
        }

    }


}
