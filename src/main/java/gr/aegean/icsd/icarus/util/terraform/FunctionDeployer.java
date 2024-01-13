package gr.aegean.icsd.icarus.util.terraform;

import com.hashicorp.cdktf.App;
import gr.aegean.icsd.icarus.provideraccount.AwsAccount;
import gr.aegean.icsd.icarus.provideraccount.GcpAccount;
import gr.aegean.icsd.icarus.provideraccount.ProviderAccount;
import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.util.services.FileService;
import gr.aegean.icsd.icarus.util.services.ProcessService;
import gr.aegean.icsd.icarus.util.aws.AwsRegion;
import gr.aegean.icsd.icarus.util.aws.LambdaRuntime;
import gr.aegean.icsd.icarus.util.enums.Platform;
import gr.aegean.icsd.icarus.util.exceptions.async.StackDeploymentException;
import gr.aegean.icsd.icarus.util.gcp.GcfRuntime;
import gr.aegean.icsd.icarus.util.gcp.GcpRegion;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static gr.aegean.icsd.icarus.util.terraform.TerraformConfiguration.STACK_OUTPUT_DIRECTORY;


@Service
@Validated
@Transactional
public class FunctionDeployer {


    private final FileService fileService;
    private final ProcessService processService;

    private static final Logger log = LoggerFactory.getLogger("Stack Deployer");



    public FunctionDeployer(FileService fileService, ProcessService processService) {
        this.fileService = fileService;
        this.processService = processService;
    }



    @Async
    public CompletableFuture<Set<DeploymentRecord>> deployFunctions(@NotNull Test associatedTest,
                                                                    @NotBlank String id) {

        String name = associatedTest.getTargetFunction().getName() + "-" + id;

        String outputDir = STACK_OUTPUT_DIRECTORY + "\\" + name;
        String stackDir = outputDir + "\\stacks\\" + name;

        // Create Terraform Stacks
        Set<DeploymentRecord> incompleteDeploymentRecords = createInfrastructure(outputDir, name,
                associatedTest, id);

        log.warn("Finished synthesizing: {}", id);

        // Deploy Terraform Stacks
        log.warn("Deploying stack: {}", id);
        deployInfrastructure(stackDir);

        log.warn("Finished deploying: {}", id);

        // Get URLs of Deployed Stacks
        log.warn("Getting function URLs for: {}", id);
        Set<DeploymentRecord> completeDeploymentRecords = matchInfrastructureWithRecords
                (stackDir, incompleteDeploymentRecords);

        return CompletableFuture.completedFuture(completeDeploymentRecords);
    }


    private synchronized Set<DeploymentRecord> createInfrastructure(String outputDir, String stackName,
                                                                    Test associatedTest, String id) {

        App app = App.Builder.create()
                .outdir(outputDir)
                .build();

        // Create stack
        log.warn("Creating stack: {}", id);
        Set<DeploymentRecord> incompleteDeploymentRecords = createTerraformStack(stackName, app, outputDir, associatedTest, id);

        log.warn("Finished creating: {}", id);

        // Synthesize it
        log.warn("Synthesizing stack: {}", id);
        app.synth();

        return incompleteDeploymentRecords;
    }


    private void deployInfrastructure(@NotBlank String stackDir) {

        File stackDirectory = new File(stackDir);

        try {

            // Initialize terraform
            log.warn("Initializing terraform...");
            processService.createProcess(stackDirectory, TerraformCommand.INIT.get());

            // Deploy infrastructure
            log.warn("Deploying infrastructure...");
            processService.createProcess(stackDirectory, TerraformCommand.APPLY.get());
        }
        catch (RuntimeException ex) {
            log.error("Error when deploying stack at: {}", stackDir);
            throw new StackDeploymentException(ex.getMessage());
        }


    }


    private Set<DeploymentRecord> matchInfrastructureWithRecords(@NotBlank String stackDirectory,
                                                                 @NotNull Set<DeploymentRecord> deploymentRecords) {

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
            StringBuilder rawTerraformOutputs = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                rawTerraformOutputs.append(line).append("\n");
            }

            return extractUrlsFromTerraformOutputs(deploymentRecords, rawTerraformOutputs.toString());
        }
        catch (IOException ex) {
            log.error(ex.getMessage());
        }


        File errorFile = new File(processBuilder.directory().getPath() + "\\output-errors.txt");
        processBuilder.redirectError(errorFile);

        throw new StackDeploymentException(errorFile, TerraformCommand.OUTPUT.get());
    }



    public void deleteInfrastructure(@NotBlank String stackName, @NotBlank String deploymentId) {

        String name = stackName + "-" + deploymentId;
        String outputDir = STACK_OUTPUT_DIRECTORY + "\\" + name;
        String stackDir = outputDir + "\\stacks\\" + name;

        File stackDirectory = new File(stackDir);

        processService.createProcess(stackDirectory, TerraformCommand.DESTROY.get());
        fileService.deleteDirectory(outputDir);
    }


    private Set<DeploymentRecord> createTerraformStack(@NotBlank String name, @NotNull App app,
                                                       @NotBlank String outputDir,
                                                       @NotNull Test associatedTest, @NotBlank String id) {

        MainStack mainStack = new MainStack(app, name);

        mainStack.addOverride("terraform.backend", Map.of(
                "local", Map.of(
                        "path", outputDir + "/" + name + ".tfstate"
                )
        ));

        mainStack.addOverride("terraform.required_providers", Map.of(
                "aws", Map.of(
                        "source", "hashicorp/aws",
                        "version", ">= 5.0"
                ),
                "google", Map.of(
                        "source", "hashicorp/google",
                        "version", ">= 5.0"
                )
        ));

        Set<DeploymentRecord> deploymentRecordSet = new HashSet<>();

        for (ProviderAccount account : associatedTest.getAccountsList()) {

            for (ResourceConfiguration configuration : associatedTest.getResourceConfigurations()) {

                if (account instanceof AwsAccount awsAccount &&
                        configuration.getProviderPlatform().equals(Platform.AWS)) {

                    AwsConstruct newAwsConstruct = mainStack.createAwsConstruct(id,
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

                    Set<DeploymentRecord> awsConstructsRecords = new HashSet<>
                            (newAwsConstruct.getDeploymentRecords());

                    addConfigurationAndAccountToRecords(awsConstructsRecords, configuration, account);

                    deploymentRecordSet.addAll(awsConstructsRecords);
                }

                if (account instanceof GcpAccount gcpAccount &&
                        configuration.getProviderPlatform().equals(Platform.GCP)) {

                    GcpConstruct newGcpConstruct = mainStack.createGcpConstruct(id,
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

                    Set<DeploymentRecord> gcpConstructsRecords = new HashSet<>
                            (newGcpConstruct.getDeploymentRecords());

                    addConfigurationAndAccountToRecords(gcpConstructsRecords, configuration, account);

                    deploymentRecordSet.addAll(gcpConstructsRecords);
                }
            }
        }

        return deploymentRecordSet;
    }


    private Set<DeploymentRecord> extractUrlsFromTerraformOutputs(@NotNull Set<DeploymentRecord> deploymentRecords,
                                                                  @NotBlank String terraformOutputs) {

        String[] lines = terraformOutputs.split("\\n");

        for (String line : lines) {

            String[] parts = line.split("=");
            String terraformOutputName = parts[0].trim();
            String functionUrl = parts[1].trim().replace("\"", "");

            for (DeploymentRecord deploymentRecord : deploymentRecords) {

                // Using contains instead of equals since terraform appends a unique id
                // in the final output name
                if(terraformOutputName.contains(deploymentRecord.deployedFunctionName)) {

                    deploymentRecord.deployedUrl = functionUrl;
                }

            }

        }

        return deploymentRecords;
    }


    private void addConfigurationAndAccountToRecords(@NotNull Set<DeploymentRecord> deploymentRecordSet,
                                                     @NotNull ResourceConfiguration configuration,
                                                     @NotNull ProviderAccount accountUsed) {

        for (DeploymentRecord deploymentRecord : deploymentRecordSet) {

            deploymentRecord.accountUsed = accountUsed;

            deploymentRecord.configurationUsed = configuration;
        }
    }


}
