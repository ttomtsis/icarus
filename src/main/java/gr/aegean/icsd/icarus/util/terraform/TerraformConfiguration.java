package gr.aegean.icsd.icarus.util.terraform;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "terraform")
public class TerraformConfiguration {


    public static String STACK_OUTPUT_DIRECTORY;


    @Value("${terraform.stackOutputDirectory}")
    public void setStackOutputDirectory(String stackOutputDirectory) {
        TerraformConfiguration.STACK_OUTPUT_DIRECTORY = stackOutputDirectory;
    }


}
