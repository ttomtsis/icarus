package gr.aegean.icsd.icarus.util.terraform;

import com.hashicorp.cdktf.App;
import gr.aegean.icsd.icarus.provideraccount.AwsAccount;
import gr.aegean.icsd.icarus.provideraccount.GcpAccount;
import gr.aegean.icsd.icarus.provideraccount.ProviderAccount;
import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.util.aws.AwsRegion;
import gr.aegean.icsd.icarus.util.aws.LambdaRuntime;
import gr.aegean.icsd.icarus.util.enums.Platform;
import gr.aegean.icsd.icarus.util.exceptions.StackDeploymentException;
import gr.aegean.icsd.icarus.util.exceptions.test.TestExecutionFailedException;
import gr.aegean.icsd.icarus.util.gcp.GcfRuntime;
import gr.aegean.icsd.icarus.util.gcp.GcpRegion;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.*;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static gr.aegean.icsd.icarus.util.terraform.TerraformConfiguration.STACK_OUTPUT_DIRECTORY;


@Service
@Validated
@Transactional
public class StackDeployer {


    private static final Logger log = LoggerFactory.getLogger("Stack Deployer");


    @Async
    public CompletableFuture<String> deploy(@NotNull Test associatedTest) {

        String name = associatedTest.getTargetFunction().getName() + UUID.randomUUID().toString().substring(0, 5);
        String outputDir = STACK_OUTPUT_DIRECTORY + "\\" + name;
        String stackDir = outputDir + "\\stacks\\" + name;

        App app = App.Builder.create()
                .outdir(outputDir)
                .build();

        // Create stack
        createStack(name, app, associatedTest);
        log.warn("Finished creating");

        // Synthesize it
        app.synth();
        log.warn("Finished synthesizing");

        // Deploy
        deployStack(stackDir);
        log.warn("Finished deploying");

        // Get functionUrls
        String functionUrls = getFunctionUrls(stackDir);

        return CompletableFuture.completedFuture(functionUrls);
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

    private void deployStack(String stackDir) {

            File terraformDirectory = new File(stackDir + "\\.terraform");
            File stackDirectory = new File(stackDir);

            // Initialize terraform
            if (!terraformDirectory.exists()) {

                createProcess(stackDirectory, TerraformCommand.INIT.get());
            }

            // Deploy infrastructure
            createProcess(stackDirectory, TerraformCommand.APPLY.get());
    }

    public void deleteStack(String stackName) {

        String outputDir = STACK_OUTPUT_DIRECTORY + "\\" + stackName;
        String stackDir = outputDir + "\\stacks\\" + stackName;
        File stackDirectory = new File(stackDir);

        createProcess(stackDirectory, TerraformCommand.DESTROY.get());
    }

    private String getFunctionUrls(String stackDirectory) {

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(stackDirectory));
        processBuilder.command(TerraformCommand.OUTPUT.get());

        try {
            Process outputProcess = processBuilder.start();

            // Get the input stream
            InputStream inputStream = outputProcess.getInputStream();
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);

            // Read and print each line of the output
            return br.readLine();
        }
        catch (IOException ex) {

            log.error(ex.getMessage());
        }

        File errorFile = new File(processBuilder.directory().getPath() + "\\output-errors.txt");
        processBuilder.redirectError(errorFile);

        throw new StackDeploymentException(errorFile, TerraformCommand.OUTPUT.get());
    }


    private void createProcess(File stackDirectory, String ... commands) {

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(stackDirectory);
        processBuilder.command(commands);

        String commandString = "\\" + String.join(" ", commands);
        commandString = commandString.replace(" ", "_").replace("-", "_");

        File outputFile = new File(processBuilder.directory().getPath() + commandString + "output.txt");
        processBuilder.redirectOutput(outputFile);

        File errorFile = new File(processBuilder.directory().getPath() + commandString + "error_output.txt");
        processBuilder.redirectError(errorFile);

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new StackDeploymentException(errorFile, exitCode, commands);
            }
        }
        catch (InterruptedException | IOException ex) {
            throw new TestExecutionFailedException(ex);
        }

    }


}
