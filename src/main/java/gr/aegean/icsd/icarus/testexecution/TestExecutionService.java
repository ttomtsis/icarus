package gr.aegean.icsd.icarus.testexecution;

import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import gr.aegean.icsd.icarus.report.Report;
import gr.aegean.icsd.icarus.report.ReportRepository;
import gr.aegean.icsd.icarus.report.ReportService;
import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.testexecution.metricresult.MetricResult;
import gr.aegean.icsd.icarus.testexecution.metricresult.MetricResultRepository;
import gr.aegean.icsd.icarus.testexecution.testcaseresult.TestCaseResult;
import gr.aegean.icsd.icarus.testexecution.testcaseresult.TestCaseResultRepository;
import gr.aegean.icsd.icarus.util.enums.ExecutionState;
import gr.aegean.icsd.icarus.util.exceptions.async.AsyncExecutionFailedException;
import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.entity.ReportGenerationException;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import gr.aegean.icsd.icarus.util.services.MathService;
import gr.aegean.icsd.icarus.util.terraform.FunctionDeployer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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

    private final FunctionDeployer deployer;

    private final ReportService reportService;
    private final ReportRepository reportRepository;

    private final MathService mathService;



    public TestExecutionService(TestExecutionRepository repository, TestRepository testRepository,
                                MetricResultRepository metricResultRepository,
                                TestCaseResultRepository testCaseResultRepository,
                                FunctionDeployer deployer, ReportService reportService,
                                ReportRepository reportRepository, MathService mathService) {

        this.testExecutionRepository = repository;
        this.testRepository = testRepository;
        this.metricResultRepository = metricResultRepository;
        this.testCaseResultRepository = testCaseResultRepository;
        this.deployer = deployer;
        this.reportService = reportService;
        this.reportRepository = reportRepository;
        this.mathService = mathService;
    }


    public TestExecution createEmptyExecution(@NotNull Test requestedTest, @NotBlank String deploymentId
            , @NotNull IcarusUser creator) {

        TestExecution newTestExecution = new TestExecution(requestedTest, Instant.now(), deploymentId, creator);
        return testExecutionRepository.save(newTestExecution);
    }


    public void saveMetricResults(TestExecution testExecution, Set<MetricResult> metricResults) {

        Instant endDate = Instant.now();
        testExecution.setEndDate(endDate);

        Set<MetricResult> resultSet = new HashSet<>(metricResultRepository.saveAllAndFlush(metricResults));
        testExecution.addMetricResults(resultSet);

        testExecution.setRegressionEquation(mathService.applyLinearRegression(testExecution));

        testExecutionRepository.save(testExecution);
    }


    public void saveTestCaseResults(TestExecution testExecution, Set<TestCaseResult> testCaseResults) {

        Instant endDate = Instant.now();
        testExecution.setEndDate(endDate);

        Set<TestCaseResult> resultSet = new HashSet<>(testCaseResultRepository.saveAll(testCaseResults));
        testExecution.addTestCaseResults(resultSet);

        testExecutionRepository.save(testExecution);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void produceReport(@NotNull TestExecution testExecution) {

        LoggerFactory.getLogger(TestExecutionService.class).warn
                ("Producing report document for deployment: {} and Execution: {}",
                        testExecution.getDeploymentId(), testExecution.getId());

        try {

            if (testExecution.getTestCaseResults().isEmpty()) {
                @NotNull Report metricResultsReport = reportService.createPerformanceTestReport(testExecution);
                reportRepository.save(metricResultsReport);

            } else if (testExecution.getMetricResults().isEmpty()) {
                @NotNull Report testCaseResultReport = reportService.createFunctionalTestReport(testExecution);
                reportRepository.save(testCaseResultReport);

            } else {
                throw new ReportGenerationException("The test execution does not contain any " +
                        "results in order to produce a report");
            }
        }
        catch (RuntimeException ex) {
            LoggerFactory.getLogger(TestExecutionService.class).error("Failed to generate the report for" +
                    " Execution with deployment ID: {}", testExecution.getDeploymentId());

            testExecution.setState(ExecutionState.REPORT_FAILED);
            testExecutionRepository.save(testExecution);

            throw new AsyncExecutionFailedException("Failed to generate Report for Execution with deployment ID: "
                    + testExecution.getDeploymentId(), ex);
        }

    }


    public Page<TestExecution> getExecutions(@NotNull @Positive Long testId, @NotNull Pageable pageable) {

        Test parentTest = checkIfTestExists(testId);

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();
        return testExecutionRepository.findAllByParentTestAndCreator(parentTest, loggedInUser, pageable);
    }


    public TestExecution getExecution(@NotNull @Positive Long testId, @NotNull @Positive Long executionId) {

        Test parentTest = checkIfTestExists(testId);

        return checkIfTestExecutionExists(parentTest, executionId);
    }


    public void deleteExecution(@NotNull @Positive Long testId, @NotNull @Positive Long executionId) {

        Test parentTest = checkIfTestExists(testId);

        TestExecution execution = checkIfTestExecutionExists(parentTest, executionId);

        testExecutionRepository.delete(execution);
    }


    public String getExecutionState(@NotNull @Positive Long testId, @NotBlank String deploymentId) {

        Test parentTest = checkIfTestExists(testId);

        TestExecution execution = testExecutionRepository.findTestExecutionByDeploymentIdAndParentTestAndCreator
                        (deploymentId, parentTest, UserUtils.getLoggedInUser())
                .orElseThrow( () -> new EntityNotFoundException(TestExecution.class, deploymentId));

       return execution.getState().toString();
    }



    public void abortTestExecution(@NotNull TestExecution requestedTestExecution, @NotBlank String deploymentId) {

        setExecutionState(requestedTestExecution, ExecutionState.ERROR);

        try {
            deployer.deleteInfrastructure(requestedTestExecution.getParentTest().getTargetFunction().getName(), deploymentId);
        }
        catch (RuntimeException ex) {
            throw new AsyncExecutionFailedException(ex);
        }
    }


    public void finalizeTestExecution(@NotNull TestExecution requestedTestExecution, @NotBlank String deploymentId) {

        deployer.deleteInfrastructure(requestedTestExecution.getParentTest().getTargetFunction().getName(), deploymentId);
        setExecutionState(requestedTestExecution, ExecutionState.FINISHED);
    }


    public void setExecutionState(@NotNull TestExecution testExecution, @NotNull ExecutionState testState) {

        testExecution.setState(testState);
        testExecutionRepository.save(testExecution);
    }



    private Test checkIfTestExists(@NotNull @Positive Long parentTestId) {

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();
        return testRepository.findTestByIdAndCreator(parentTestId, loggedInUser)
                .orElseThrow( () -> new EntityNotFoundException(Test.class, parentTestId));
    }

    private TestExecution checkIfTestExecutionExists(@NotNull Test parentTest, @NotNull @Positive Long executionId) {

        return testExecutionRepository.findTestExecutionByIdAndParentTestAndCreator
                        (executionId, parentTest, UserUtils.getLoggedInUser())
                .orElseThrow( () -> new EntityNotFoundException(TestExecution.class, executionId));
    }


}
