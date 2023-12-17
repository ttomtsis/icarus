package gr.aegean.icsd.icarus.util.terraform;

import com.hashicorp.cdktf.App;
import gr.aegean.icsd.icarus.provideraccount.AwsAccount;
import gr.aegean.icsd.icarus.provideraccount.GcpAccount;
import gr.aegean.icsd.icarus.provideraccount.ProviderAccount;
import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.util.aws.AwsRegion;
import gr.aegean.icsd.icarus.util.aws.LambdaRuntime;
import gr.aegean.icsd.icarus.util.enums.Platform;
import gr.aegean.icsd.icarus.util.gcp.GcfRuntime;
import gr.aegean.icsd.icarus.util.gcp.GcpRegion;
import jakarta.validation.constraints.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.stream.Collectors;

import static gr.aegean.icsd.icarus.util.terraform.TerraformConfiguration.STACK_OUTPUT_DIRECTORY;


@Service
public class StackDeployer {


    @Async
    public void deploy(@NotNull Test associatedTest) {

        String name = associatedTest.getTargetFunction().getName();
        String outputDir = STACK_OUTPUT_DIRECTORY + "/" + name;

        App app = App.Builder.create()
                .outdir(outputDir)
                .build();

        createStack(name, app, associatedTest);

        app.synth();

        LoggerFactory.getLogger("Stack Deployer").warn("Finished synthesizing");

    }

    private void createStack(String name, App app, Test associatedTest) {

        MainStack mainStack = new MainStack(app, name);

        for (ProviderAccount account : associatedTest.getAccountsList()) {

            for (ResourceConfiguration configuration : associatedTest.getResourceConfigurations()) {

                if (account.getAccountType().equals("AwsAccount") &&
                        configuration.getProviderPlatform().equals(Platform.AWS)) {

                    AwsAccount awsAccount = (AwsAccount) account;

                    mainStack.createAwsConstruct(
                            awsAccount.getAwsAccessKey(), awsAccount.getAwsSecretKey(),
                            associatedTest.getTargetFunction().getFunctionSourceDirectory(),
                            associatedTest.getTargetFunction().getFunctionSourceFileName(),
                            configuration.getRegions().stream().map(AwsRegion::valueOf)
                                    .collect(Collectors.toCollection(HashSet::new)),
                            configuration.getMemoryConfigurations(),
                            associatedTest.getTargetFunction().getName(),
                            LambdaRuntime.valueOf(configuration.getFunctionRuntime()),
                            associatedTest.getTargetFunction().getFunctionHandler(),
                            associatedTest.getPath(),
                            associatedTest.getHttpMethod()
                    );
                }

                if (account.getAccountType().equals("GcpAccount") &&
                        configuration.getProviderPlatform().equals(Platform.GCP)) {

                    GcpAccount gcpAccount = (GcpAccount) account;

                    mainStack.createGcpConstruct(
                            gcpAccount.getGcpKeyfile(),
                            associatedTest.getTargetFunction().getFunctionSourceDirectory(),
                            associatedTest.getTargetFunction().getFunctionSourceFileName(),
                            associatedTest.getTargetFunction().getName(),
                            associatedTest.getTargetFunction().getDescription(),
                            gcpAccount.getGcpProjectId(),
                            GcfRuntime.valueOf(configuration.getFunctionRuntime()),
                            associatedTest.getTargetFunction().getFunctionHandler(),
                            configuration.getMemoryConfigurations(),
                            configuration.getCpuConfigurations(),
                            configuration.getRegions().stream().map(GcpRegion::valueOf)
                                    .collect(Collectors.toCollection(HashSet::new))
                    );
                }
            }
        }

    }


}
