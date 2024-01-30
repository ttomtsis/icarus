package gr.aegean.icsd.icarus;

import gr.aegean.icsd.icarus.util.exceptions.IcarusConfigurationException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Level;


@Configuration
public class IcarusConfiguration {


    public static String FUNCTION_SOURCES_DIRECTORY;

    public static String TEST_CASE_RESULT_REPORT_TEMPLATE_LOCATION;
    public static String METRIC_RESULT_REPORT_TEMPLATE_LOCATION;



    public IcarusConfiguration(@Value("${icarus.reports.metricResultReportTemplateLocation}")
                               String metricResultReportTemplateLocation,

                               @Value("${icarus.reports.testCaseResultReportTemplateLocation}")
                               String testCaseResultReportTemplateLocation,

                               @Value("${icarus.functionSourcesDirectory}")
                               String functionSourcesDirectory) {

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

            throw new IcarusConfigurationException("Could not start the Birt platform.", e);
        }

        IReportEngineFactory factory =
                (IReportEngineFactory) Platform
                        .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);

        return factory.createReportEngine(config);
    }


    @Bean
    public void checkIcarusConfiguration() {
        //TODO: Check if terraform and jmeter can run, check configuration properties values
    }


}
