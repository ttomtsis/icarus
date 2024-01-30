package gr.aegean.icsd.icarus;

import gr.aegean.icsd.icarus.util.exceptions.IcarusConfigurationException;
import gr.aegean.icsd.icarus.util.services.ProcessService;
import org.apache.jmeter.util.JMeterUtils;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.logging.Level;

import static gr.aegean.icsd.icarus.util.jmeter.JMeterConfiguration.JMETER_HOME_DIRECTORY;
import static gr.aegean.icsd.icarus.util.jmeter.JMeterConfiguration.JMETER_PROPERTIES_FILE;


@Configuration
public class IcarusConfiguration {


    private static final Logger log = LoggerFactory.getLogger(IcarusConfiguration.class);

    private final ProcessService processService;


    public static String FUNCTION_SOURCES_DIRECTORY;
    public static String TEST_CASE_RESULT_REPORT_TEMPLATE_LOCATION;
    public static String METRIC_RESULT_REPORT_TEMPLATE_LOCATION;



    public IcarusConfiguration(ProcessService processService,

                               @Value("${icarus.reports.metricResultReportTemplateLocation}")
                               String metricResultReportTemplateLocation,

                               @Value("${icarus.reports.testCaseResultReportTemplateLocation}")
                               String testCaseResultReportTemplateLocation,

                               @Value("${icarus.functionSourcesDirectory}")
                               String functionSourcesDirectory) {

        this.processService = processService;

        IcarusConfiguration.FUNCTION_SOURCES_DIRECTORY = functionSourcesDirectory;
        IcarusConfiguration.TEST_CASE_RESULT_REPORT_TEMPLATE_LOCATION = testCaseResultReportTemplateLocation;
        IcarusConfiguration.METRIC_RESULT_REPORT_TEMPLATE_LOCATION = metricResultReportTemplateLocation;
    }



    @Bean
    public IReportEngine initializeBirtEngine() {

        EngineConfig config = new EngineConfig();
        config.setLogConfig(null, Level.OFF);

        try {

            Platform.startup(config);

        } catch (Exception e) {
            log.error("Unable to start the BiRT Engine: {}", e.getMessage());
            throw new IcarusConfigurationException("Could not start the Birt platform.", e);
        }

        IReportEngineFactory factory =
                (IReportEngineFactory) Platform
                        .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);

        return factory.createReportEngine(config);
    }


    @Bean
    public boolean checkIcarusConfiguration(){

        log.info("Checking terraform version");
        try {
            String terraformVersion = processService.runCommand("terraform", "-v");
            log.info("Terraform version:\n{}", terraformVersion);
        }
        catch (IOException | InterruptedException ex) {
            log.error("Fatal error: Icarus cannot find Terraform: {}", ex.getMessage());
            throw new IcarusConfigurationException("Terraform not found");
        }

        log.info("Validating JMeter's configuration directory");
        try {
            JMeterUtils.setJMeterHome(JMETER_HOME_DIRECTORY);
            JMeterUtils.loadJMeterProperties(JMETER_PROPERTIES_FILE);
            JMeterUtils.initLocale();

            log.info("JMeter's configuration directory is valid and located at: {}", JMETER_PROPERTIES_FILE);
        } catch (Exception e) {
            log.error("Error initializing JMeter: {}", e.getMessage());
            throw new IcarusConfigurationException("Unable to initialize JMeter");
        }

        return true;
    }


}
