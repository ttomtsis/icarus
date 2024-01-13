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
import gr.aegean.icsd.icarus.util.exceptions.async.StackDeploymentException;
import gr.aegean.icsd.icarus.util.exceptions.async.TestExecutionFailedException;
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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static gr.aegean.icsd.icarus.util.terraform.TerraformConfiguration.STACK_OUTPUT_DIRECTORY;


@Service
@Validated
@Transactional
public class StackDeployer {


    private static final Logger log = LoggerFactory.getLogger("Stack Deployer");



    @Async
    public CompletableFuture<Set<DeploymentRecord>> deploy(@NotNull Test associatedTest,
                                                           @NotBlank String id) {

        String name = associatedTest.getTargetFunction().getName() + "-" + id;

        String outputDir = STACK_OUTPUT_DIRECTORY + "\\" + name;
        String stackDir = outputDir + "\\stacks\\" + name;

        Set<DeploymentRecord> incompleteDeploymentRecords = createTerraformConfiguration(outputDir, name,
                associatedTest, id);

        log.warn("Finished synthesizing: {}", id);

        // Deploy
        log.warn("Deploying stack: {}", id);
        deployStack(stackDir);

        log.warn("Finished deploying: {}", id);

        // Get functionUrls
        log.warn("Getting function URLs for: {}", id);
        Set<DeploymentRecord> completeDeploymentRecords = getFunctionUrls(stackDir, incompleteDeploymentRecords);

        return CompletableFuture.completedFuture(completeDeploymentRecords);
    }

    private synchronized Set<DeploymentRecord> createTerraformConfiguration(String outputDir, String stackName,
                                                                            Test associatedTest, String id) {

        App app = App.Builder.create()
                .outdir(outputDir)
                .build();

        // Create stack
        log.warn("Creating stack: {}", id);
        Set<DeploymentRecord> incompleteDeploymentRecords = createStack(stackName, app, outputDir, associatedTest, id);

        log.warn("Finished creating: {}", id);

        // Synthesize it
        log.warn("Synthesizing stack: {}", id);
        app.synth();

        return incompleteDeploymentRecords;
    }

    private Set<DeploymentRecord> createStack(@NotBlank String name, @NotNull App app, @NotBlank String outputDir,
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


    private void deployStack(@NotBlank String stackDir) {

        File stackDirectory = new File(stackDir);

        try {

            // Initialize terraform
            log.warn("Initializing terraform...");
            createProcess(stackDirectory, TerraformCommand.INIT.get());

            // Deploy infrastructure
            log.warn("Deploying infrastructure...");
            createProcess(stackDirectory, TerraformCommand.APPLY.get());
        }
        catch (RuntimeException ex) {
            log.error("Error when deploying stack at: {}", stackDir);
            throw new StackDeploymentException(ex.getMessage());
        }


    }


    public void deleteStack(@NotBlank String stackName, @NotBlank String deploymentId) {

        String name = stackName + "-" + deploymentId;
        String outputDir = STACK_OUTPUT_DIRECTORY + "\\" + name;
        String stackDir = outputDir + "\\stacks\\" + name;

        File stackDirectory = new File(stackDir);

        createProcess(stackDirectory, TerraformCommand.DESTROY.get());
        deleteDirectory(outputDir);
    }


    private void deleteDirectory(String dir) {

        try {

            Files.walkFileTree(Path.of(dir), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });

        }
        catch (IOException ex) {
            throw new TestExecutionFailedException(ex);
        }
    }


    private Set<DeploymentRecord> getFunctionUrls(@NotBlank String stackDirectory,
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

            return extractUrls(deploymentRecords, rawTerraformOutputs.toString());
        }
        catch (IOException ex) {
            log.error(ex.getMessage());
        }


        File errorFile = new File(processBuilder.directory().getPath() + "\\output-errors.txt");
        processBuilder.redirectError(errorFile);

        throw new StackDeploymentException(errorFile, TerraformCommand.OUTPUT.get());
    }


    private Set<DeploymentRecord> extractUrls(@NotNull Set<DeploymentRecord> deploymentRecords,
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


    private void createProcess(@NotNull File stackDirectory, @NotNull String ... commands) {

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(stackDirectory);
        processBuilder.command(commands);

        String commandString = "_" + commands[1];

        log.warn("executing command: {}", Arrays.toString(commands));

        File outputFile = new File(processBuilder.directory().getPath() + commandString + "_output.txt");
        processBuilder.redirectOutput(outputFile);

        File errorFile = new File(processBuilder.directory().getPath() + commandString + "_error_output.txt");
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


    private void addConfigurationAndAccountToRecords(@NotNull Set<DeploymentRecord> deploymentRecordSet,
                                                     @NotNull ResourceConfiguration configuration,
                                                     @NotNull ProviderAccount accountUsed) {

        for (DeploymentRecord deploymentRecord : deploymentRecordSet) {

            deploymentRecord.accountUsed = accountUsed;

            deploymentRecord.configurationUsed = configuration;
        }
    }

}
