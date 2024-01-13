package gr.aegean.icsd.icarus.report;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.test.functionaltest.FunctionalTest;
import gr.aegean.icsd.icarus.test.performancetest.PerformanceTest;
import gr.aegean.icsd.icarus.testexecution.TestExecution;
import gr.aegean.icsd.icarus.testexecution.TestExecutionRepository;
import gr.aegean.icsd.icarus.util.annotations.ValidFilePath.ValidFilePath;
import gr.aegean.icsd.icarus.util.enums.ExecutionState;
import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.entity.ReportGenerationException;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.eclipse.birt.report.engine.api.*;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static gr.aegean.icsd.icarus.IcarusConfiguration.METRIC_RESULT_REPORT_TEMPLATE_LOCATION;
import static gr.aegean.icsd.icarus.IcarusConfiguration.TEST_CASE_RESULT_REPORT_TEMPLATE_LOCATION;


@Service
@Transactional
@Validated
public class ReportService {


    private final IReportEngine birtReportEngine;
    private final TestRepository testRepository;
    private final TestExecutionRepository testExecutionRepository;
    private final ReportRepository reportRepository;



    public ReportService(IReportEngine reportEngine, TestRepository testRepository,
                         TestExecutionRepository testExecutionRepository,
                         ReportRepository reportRepository) {

        this.birtReportEngine = reportEngine;
        this.testRepository = testRepository;
        this.testExecutionRepository = testExecutionRepository;
        this.reportRepository = reportRepository;
    }



    public Report getReport(@NotNull @Positive Long testId, @NotNull @Positive Long executionID) {

        TestExecution parentExecution = findExecution(testId, executionID);

        checkExecutionStatus(parentExecution);

        return parentExecution.getReport();
    }


    public Report getReport(@NotNull @Positive Long testId, @NotBlank String deploymentId) {

        TestExecution associatedExecution = findExecution(testId, deploymentId);

        checkExecutionStatus(associatedExecution);

        return associatedExecution.getReport();
    }


    public Report regenerateReportByID(@NotNull @Positive Long testId, @NotBlank String deploymentId) {

        TestExecution associatedExecution = findExecution(testId, deploymentId);

        return regenerateReport(associatedExecution);
    }


    public Report regenerateReportByID(@NotNull @Positive Long testId, @NotNull @Positive Long executionID) {

        TestExecution associatedExecution = findExecution(testId, executionID);

        return regenerateReport(associatedExecution);
    }


    public Report regenerateReport(TestExecution associatedExecution) {

        if (associatedExecution.getReport() != null) {
            throw new ReportGenerationException("Test execution with deployment ID: " +
                    associatedExecution.getDeploymentId() + " and ID: " + associatedExecution.getId() +
                    " already has a Report associated with it");
        }

        if (!associatedExecution.getState().equals(ExecutionState.REPORT_FAILED)) {
            throw new ReportGenerationException("Test execution with deployment ID: " +
                    associatedExecution.getDeploymentId() + " and ID: " + associatedExecution.getId() +
                    " is in an invalid state and cannot regenerate it's report.\n" +
                    "State: " + associatedExecution.getState());
        }

        try {
            if (associatedExecution.getParentTest() instanceof FunctionalTest) {

                Report regeneratedReport = createFunctionalTestReport(associatedExecution);
                associatedExecution.setState(ExecutionState.FINISHED);
                reportRepository.save(regeneratedReport);
                testExecutionRepository.save(associatedExecution);

                return regeneratedReport;

            } else if (associatedExecution.getParentTest() instanceof PerformanceTest) {

                Report regeneratedReport = createPerformanceTestReport(associatedExecution);
                associatedExecution.setState(ExecutionState.FINISHED);
                reportRepository.save(regeneratedReport);
                testExecutionRepository.save(associatedExecution);

                return regeneratedReport;

            } else {
                throw new ReportGenerationException("Test execution with deployment ID: " + associatedExecution.getDeploymentId()
                + " and ID: " + associatedExecution.getId() + " failed to generate a Report." +
                        "\nThe Test associated is of unknown type");
            }
        }
        catch (RuntimeException ex) {
            associatedExecution.setState(ExecutionState.REPORT_FAILED);
            testExecutionRepository.save(associatedExecution);
            throw new ReportGenerationException(associatedExecution.getDeploymentId(), ex);
        }

    }


    public Report createFunctionalTestReport(@NotNull TestExecution execution) {

        String documentName = execution.getParentTest().getName() + "-"
                + execution.getDeploymentId() + "-Report.pdf";

        Map<String, Object> parameters = addParameters(execution);

        byte[] documentReport = generateReport(parameters, TEST_CASE_RESULT_REPORT_TEMPLATE_LOCATION);

        return new Report(execution, documentReport, documentName);
    }


    public Report createPerformanceTestReport(@NotNull TestExecution execution) {

        String documentName = execution.getParentTest().getName() + "-"
                + execution.getDeploymentId() + "-Report.pdf";

        Map<String, Object> parameters = addParameters(execution);

        byte[] documentReport = generateReport(parameters, METRIC_RESULT_REPORT_TEMPLATE_LOCATION);

        return new Report(execution, documentReport, documentName);
    }



    private Map<String, Object> addParameters(TestExecution execution) {

        Map<String, Object> parameters = new HashMap<>();

        parameters.put("ExecutionID", "" + execution.getId());
        parameters.put("authorUsername", execution.getCreator().getUsername());
        parameters.put("TestID", "" + execution.getParentTest().getId());
        parameters.put("FunctionID", "" + execution.getParentTest().getTargetFunction().getId());

        String formattedTimestamp = DateTimeFormatter.ofPattern("MMM dd yyyy h:mm:ss a")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());
        parameters.put("creationDate", formattedTimestamp);

        return parameters;
    }

    private byte[] generateReport(@NotNull Map<String, Object> parameters,
                                 @NotBlank @ValidFilePath String reportDesignFilePath) {

        try {
            IReportRunnable design = birtReportEngine.openReportDesign(reportDesignFilePath);
            IRunAndRenderTask task = birtReportEngine.createRunAndRenderTask(design);

            task.setParameterValues(parameters);

            PDFRenderOption options = new PDFRenderOption();
            options.setOutputFormat("pdf");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            options.setOutputStream(byteArrayOutputStream);

            task.setRenderOption(options);
            task.run();
            task.close();

            return byteArrayOutputStream.toByteArray();
        }
        catch (EngineException ex) {
            throw new ReportGenerationException(ex);
        }

    }

    private TestExecution findExecution(Long testId, String deploymentId) {

        Test associatedTest = testRepository.findTestByIdAndCreator(testId, UserUtils.getLoggedInUser())
                .orElseThrow( () -> new EntityNotFoundException(Test.class, testId));

        return testExecutionRepository
                .findTestExecutionByParentTestAndDeploymentIdAndCreator(associatedTest, deploymentId,
                        UserUtils.getLoggedInUser())
                .orElseThrow(() -> new EntityNotFoundException
                        ("Test Execution with deployment ID: " + deploymentId + " was not found"));
    }

    private TestExecution findExecution(Long testId, Long executionID) {

        Test associatedTest = testRepository.findTestByIdAndCreator(testId, UserUtils.getLoggedInUser())
                .orElseThrow( () -> new EntityNotFoundException(Test.class, testId));

        return testExecutionRepository.findTestExecutionByIdAndParentTestAndCreator
                        (executionID, associatedTest, UserUtils.getLoggedInUser())
                .orElseThrow( () -> new EntityNotFoundException(TestExecution.class, executionID));
    }

    private void checkExecutionStatus(TestExecution parentExecution) {

        if (parentExecution.getState().equals(ExecutionState.REPORT_FAILED)) {
            throw new ReportGenerationException
                    ("The Report for Test Execution: " + parentExecution.getId()
                    + " failed to generate");
        }

        if (parentExecution.getReport() == null && !parentExecution.getState().equals(ExecutionState.ERROR)) {
            throw new EntityNotFoundException
                    ("The Report for Test Execution: " + parentExecution.getId()
                            + " is not yet ready. \nExecution is still in state: " + parentExecution.getState());
        }

        else if (parentExecution.getReport() == null && parentExecution.getState().equals(ExecutionState.ERROR)) {
            throw new EntityNotFoundException
                    ("Test Execution: " + parentExecution.getId() + " failed to execute due to an error." +
                            "\nNo Report was produced");
        }
    }


}
