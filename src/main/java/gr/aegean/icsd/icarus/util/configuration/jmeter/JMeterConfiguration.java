package gr.aegean.icsd.icarus.util.configuration.jmeter;

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
    public static String jmeterHomeDirectory;

    /**
     * Location of JMeter's Properties file
     */
    public static String jmeterPropertiesFile;

    /**
     * Directory where JMeter will produce logs
     */
    public static String jmeterLogOutputDirectory;


    @Value("${jmeter.jmeterHome}")
    public void setJmeterHomeFile(String jmeterHomeFile) {
        JMeterConfiguration.jmeterHomeDirectory = jmeterHomeFile;
    }

    @Value("${jmeter.jmeterProperties}")
    public void setJmeterPropertiesFile(String jmeterPropertiesFile) {
        JMeterConfiguration.jmeterPropertiesFile = jmeterPropertiesFile;
    }

    @Value("${jmeter.jmeterLogOutputDirectory}")
    public void setJmeterLogOutputDirectory(String jmeterLogOutputDirectory) {
        JMeterConfiguration.jmeterLogOutputDirectory = jmeterLogOutputDirectory;
    }
}
