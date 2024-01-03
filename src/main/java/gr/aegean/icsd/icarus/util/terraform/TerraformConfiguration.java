package gr.aegean.icsd.icarus.util.terraform;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "terraform")
public class TerraformConfiguration {


    public static String STACK_OUTPUT_DIRECTORY;
    public static boolean USE_LOCAL_PROVIDERS;
    public static String LOCAL_PROVIDERS_DIRECTORY;



    @Value("${terraform.stackOutputDirectory}")
    public void setStackOutputDirectory(String stackOutputDirectory) {
        TerraformConfiguration.STACK_OUTPUT_DIRECTORY = stackOutputDirectory;
    }

    @Value("${terraform.localProvidersDirectory}")
    public void setLocalProviderDirectory(String localProviderDirectory) {
        TerraformConfiguration.LOCAL_PROVIDERS_DIRECTORY = localProviderDirectory;
    }

    @Value("${terraform.useLocalProviders}")
    public void setUseLocalProviders(boolean useLocalProviders) {
        TerraformConfiguration.USE_LOCAL_PROVIDERS = useLocalProviders;
    }


}
