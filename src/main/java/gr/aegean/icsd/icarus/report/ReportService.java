package gr.aegean.icsd.icarus.report;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.testexecution.TestExecution;
import gr.aegean.icsd.icarus.testexecution.TestExecutionRepository;
import gr.aegean.icsd.icarus.util.annotations.ValidFilePath.ValidFilePath;
import gr.aegean.icsd.icarus.util.enums.TestState;
import gr.aegean.icsd.icarus.util.exceptions.async.TestExecutionFailedException;
import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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



    public ReportService(IReportEngine reportEngine, TestRepository testRepository,
                         TestExecutionRepository testExecutionRepository) {

        this.birtReportEngine = reportEngine;
        this.testRepository = testRepository;
        this.testExecutionRepository = testExecutionRepository;
    }



    public Report getReport(Long testId, Long executionID) {

        Test associatedTest = testRepository.findTestByIdAndCreator(testId, UserUtils.getLoggedInUser())
                .orElseThrow( () -> new EntityNotFoundException(Test.class, testId));

        TestExecution parentExecution = testExecutionRepository.findTestExecutionByIdAndParentTestAndCreator
                        (executionID, associatedTest, UserUtils.getLoggedInUser())
                .orElseThrow( () -> new EntityNotFoundException(TestExecution.class, executionID));

        if (parentExecution.getReport() == null && !parentExecution.getState().equals(TestState.ERROR)) {
            throw new EntityNotFoundException
                    ("The report for Test Execution: " + parentExecution.getId()
                    + " is not yet ready. \nExecution is still in state: " + parentExecution.getState());
        }

        else if (parentExecution.getReport() == null && parentExecution.getState().equals(TestState.ERROR)) {
            throw new EntityNotFoundException
                    ("Test Execution: " + parentExecution.getId() + " failed to execute due to an error." +
                            "\nNo Report was produced");
        }

        return parentExecution.getReport();
    }


    public Report getReport(Long testId, String deploymentId) {

        Test associatedTest = testRepository.findTestByIdAndCreator(testId, UserUtils.getLoggedInUser())
                .orElseThrow( () -> new EntityNotFoundException(Test.class, testId));

        TestExecution associatedExecution = testExecutionRepository
                .findTestExecutionByParentTestAndDeploymentIdAndCreator(associatedTest, deploymentId,
                        UserUtils.getLoggedInUser())
                .orElseThrow(() -> new EntityNotFoundException
                        ("Test Execution with deployment ID: " + deploymentId + " was not found"));

        if (associatedExecution.getReport() != null) {
            return associatedExecution.getReport();
        }

        throw new EntityNotFoundException("Test Execution with deployment ID: " + deploymentId +
                " does not have a Report associated with it");
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
            throw new TestExecutionFailedException(ex);
        }

    }


}
