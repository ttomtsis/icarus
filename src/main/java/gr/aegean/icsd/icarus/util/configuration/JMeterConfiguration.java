package gr.aegean.icsd.icarus.util.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * Configuration class containing constants that define JMeter's execution
 */
@Configuration
@ConfigurationProperties(prefix = "jmeter")
public class JMeterConfiguration {

    /**
     * Home Directory of JMeter
     */
    public static String JMETER_HOME_DIRECTORY;

    /**
     * Location of JMeter's Properties file
     */
    public static String JMETER_PROPERTIES_FILE;

    /**
     * Directory where JMeter will produce logs
     */
    public static String JMETER_LOG_OUTPUT_DIRECTORY;


    @Value("${jmeter.jmeterHome}")
    public void setJmeterHomeFile(String jmeterHomeFile) {
        JMeterConfiguration.JMETER_HOME_DIRECTORY = jmeterHomeFile;
    }

    @Value("${jmeter.jmeterProperties}")
    public void setJmeterPropertiesFile(String jmeterPropertiesFile) {
        JMeterConfiguration.JMETER_PROPERTIES_FILE = jmeterPropertiesFile;
    }

    @Value("${jmeter.jmeterLogOutputDirectory}")
    public void setJmeterLogOutputDirectory(String jmeterLogOutputDirectory) {
        JMeterConfiguration.JMETER_LOG_OUTPUT_DIRECTORY = jmeterLogOutputDirectory;
    }


}
