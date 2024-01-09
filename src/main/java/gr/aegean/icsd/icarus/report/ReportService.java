package gr.aegean.icsd.icarus.report;

import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.testexecution.TestExecution;
import gr.aegean.icsd.icarus.util.exceptions.async.TestExecutionFailedException;
import org.eclipse.birt.report.engine.api.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static gr.aegean.icsd.icarus.IcarusConfiguration.*;


@Service
public class ReportService {


    private final IReportEngine birtReportEngine;



    public ReportService(IReportEngine reportEngine) {
        this.birtReportEngine = reportEngine;
    }



    public Report createReport() {
        return new Report();
    }

    public Report createFunctionalTestReport(TestExecution execution, String username) {

        String outputDirectory = OUTPUT_REPORT_DIRECTORY + "\\" + username + "\\" + "Reports";
        String outputFilePath = outputDirectory + "\\" + execution.getParentTest().getName() + "-"
                + execution.getDeploymentId() + "-Report.pdf";

        Map<String, Object> parameters = new HashMap<>();

        parameters.put("ExecutionID", "" + execution.getId());
        parameters.put("authorUsername", username);

        // TODO: This will likely be cause for future issues.
        //  The root of this issue is the implementation of Functional Test
        ResourceConfiguration configuration = null;
        for (ResourceConfiguration resourceConfiguration : execution.getParentTest().getResourceConfigurations()) {
            configuration = resourceConfiguration;
        }
        parameters.put("ConfigurationID", "" + configuration.getId());

        parameters.put("TestID", "" + execution.getParentTest().getId());
        parameters.put("FunctionID", "" + execution.getParentTest().getTargetFunction().getId());

        String formattedTimestamp = DateTimeFormatter.ofPattern("MMM dd yyyy h:mm:ss a")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());
        parameters.put("creationDate", formattedTimestamp);


        generateReport(parameters, TEST_CASE_RESULT_REPORT_TEMPLATE_LOCATION, outputFilePath);


        return null;
    }

    public Report createPerformanceTestReport(TestExecution testExecution, String username) {

        String outputDirectory = OUTPUT_REPORT_DIRECTORY + "\\" + username + "\\" + "Reports";

        Map<String, Object> parameters = new HashMap<>();

        generateReport(parameters, METRIC_RESULT_REPORT_TEMPLATE_LOCATION, outputDirectory);

        return null;
    }


    public void generateReport(Map<String, Object> parameters, String reportDesignFilePath, String outputFilePath) {

        try {
            IReportRunnable design = birtReportEngine.openReportDesign(reportDesignFilePath);
            IRunAndRenderTask task = birtReportEngine.createRunAndRenderTask(design);

            task.setParameterValues(parameters);

            PDFRenderOption options = new PDFRenderOption();
            options.setOutputFileName(outputFilePath);
            options.setOutputFormat("pdf");

            task.setRenderOption(options);
            task.run();
            task.close();
        }
        catch (EngineException ex) {
            throw new TestExecutionFailedException(ex);
        }

    }


}
