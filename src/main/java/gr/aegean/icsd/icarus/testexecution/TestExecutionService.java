package gr.aegean.icsd.icarus.testexecution;

import gr.aegean.icsd.icarus.report.Report;
import gr.aegean.icsd.icarus.report.ReportService;
import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.testexecution.metricresult.MetricResult;
import gr.aegean.icsd.icarus.testexecution.metricresult.MetricResultRepository;
import gr.aegean.icsd.icarus.testexecution.testcaseresult.TestCaseResult;
import gr.aegean.icsd.icarus.testexecution.testcaseresult.TestCaseResultRepository;
import gr.aegean.icsd.icarus.user.IcarusUser;
import gr.aegean.icsd.icarus.util.enums.TestState;
import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.async.TestExecutionFailedException;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import gr.aegean.icsd.icarus.util.terraform.StackDeployer;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;


@Service
@Transactional
@Validated
public class TestExecutionService {


    private final TestExecutionRepository testExecutionRepository;
    private final TestRepository testRepository;
    private final MetricResultRepository metricResultRepository;
    private final TestCaseResultRepository testCaseResultRepository;
    private final StackDeployer deployer;
    private final ReportService reportService;



    public TestExecutionService(TestExecutionRepository repository, TestRepository testRepository,
                                MetricResultRepository metricResultRepository,
                                TestCaseResultRepository testCaseResultRepository,
                                StackDeployer deployer, ReportService reportService) {

        this.testExecutionRepository = repository;
        this.testRepository = testRepository;
        this.metricResultRepository = metricResultRepository;
        this.testCaseResultRepository = testCaseResultRepository;
        this.deployer = deployer;
        this.reportService = reportService;
    }



    public TestExecution createEmptyExecution(@NotNull Test requestedTest, @NotBlank String deploymentId) {

        TestExecution newTestExecution = new TestExecution(requestedTest, Instant.now(), deploymentId);
        return testExecutionRepository.save(newTestExecution);
    }


    public void addMetricResultsToExecution(@NotNull TestExecution testExecution,
                                            @NotNull Set<MetricResult> metricResults) {

        Instant endDate = Instant.now();
        @NotNull Report testResultsReport = reportService.createReport();

        testExecution.setEndDate(endDate);
        testExecution.setReport(testResultsReport);

        Set<MetricResult> resultSet = new HashSet<>(metricResultRepository.saveAll(metricResults));
        testExecution.addMetricResults(resultSet);

        testExecutionRepository.save(testExecution);
    }

    public void addTestCaseResultsToExecution(@NotNull TestExecution testExecution,
                                              @NotNull Set<TestCaseResult> testCaseResults) {

        Instant endDate = Instant.now();
        @NotNull Report testResultsReport = reportService.createReport();

        testExecution.setEndDate(endDate);
        testExecution.setReport(testResultsReport);

        Set<TestCaseResult> resultSet = new HashSet<>(testCaseResultRepository.saveAll(testCaseResults));
        testExecution.addTestCaseResults(resultSet);

        testExecutionRepository.save(testExecution);
    }



    public Page<TestExecution> getExecutions(@NotNull @Positive Long testId, @NotNull Pageable pageable) {

        Test parentTest = checkIfTestExists(testId);

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();
        return testExecutionRepository.findAllByParentTestAndCreator(parentTest, loggedInUser, pageable);
    }

    public TestExecution getExecution(@NotNull @Positive Long testId, @NotNull @Positive Long executionId) {

        Test parentTest = checkIfTestExists(testId);

        for (TestExecution execution : parentTest.getTestExecutions()) {
            if (execution.getId().equals(executionId)) {
                return execution;
            }
        }

        throw new EntityNotFoundException(TestExecution.class, executionId);
    }

    public void deleteExecution(@NotNull @Positive Long testId, @NotNull @Positive Long executionId) {

        Test parentTest = checkIfTestExists(testId);

        for (TestExecution execution : parentTest.getTestExecutions()) {
            if (execution.getId().equals(executionId)) {
                testExecutionRepository.delete(execution);
                return;
            }
        }

        throw new EntityNotFoundException(TestExecution.class, executionId);
    }

    public String getExecutionState(@NotNull @Positive Long testId, @NotBlank String deploymentId) {

        Test parentTest = checkIfTestExists(testId);

        for (TestExecution execution : parentTest.getTestExecutions()) {

            if (execution.getDeploymentId().equals(deploymentId)) {
                return execution.getState().toString();
            }
        }

        throw new EntityNotFoundException(TestExecution.class, deploymentId);
    }



    public void abortTestExecution(@NotNull TestExecution requestedTestExecution, @NotBlank String deploymentId) {

        setExecutionState(requestedTestExecution, TestState.ERROR);

        try {
            deployer.deleteStack(requestedTestExecution.getParentTest().getTargetFunction().getName(), deploymentId);
        }
        catch (RuntimeException ex) {
            throw new TestExecutionFailedException(ex);
        }
    }


    public void finalizeTestExecution(@NotNull TestExecution requestedTestExecution, @NotBlank String deploymentId) {

        deployer.deleteStack(requestedTestExecution.getParentTest().getTargetFunction().getName(), deploymentId);
        setExecutionState(requestedTestExecution, TestState.FINISHED);
    }


    public void setExecutionState(@NotNull TestExecution testExecution, @NotNull TestState testState) {

        testExecution.setState(testState);
        testExecutionRepository.save(testExecution);
    }



    private Test checkIfTestExists(@NotNull @Positive Long parentTestId) {

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();
        return testRepository.findTestByIdAndCreator(parentTestId, loggedInUser)
                .orElseThrow( () -> new EntityNotFoundException(Test.class, parentTestId));
    }


}
