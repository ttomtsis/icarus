package gr.aegean.icsd.icarus.util.configuration;


import org.springframework.beans.factory.annotation.Value;

public class TerraformConfiguration {


    public static String STACK_OUTPUT_DIRECTORY;


    @Value("${terraform.stackOutputDirectory}")
    public void setJmeterHomeFile(String stackOutputDirectory) {
        TerraformConfiguration.STACK_OUTPUT_DIRECTORY = stackOutputDirectory;
    }


}
