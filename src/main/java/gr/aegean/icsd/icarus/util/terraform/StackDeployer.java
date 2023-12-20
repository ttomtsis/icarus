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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static gr.aegean.icsd.icarus.util.terraform.TerraformConfiguration.STACK_OUTPUT_DIRECTORY;


@Service
@Validated
@Transactional
public class StackDeployer {


    private static final Logger log = LoggerFactory.getLogger("Stack Deployer");



    @Async
    public CompletableFuture<HashMap<CompositeKey, String>> deploy(@NotNull Test associatedTest,
                                                                   @NotBlank String id) {

        String name = associatedTest.getTargetFunction().getName() + "-" + id;

        String outputDir = STACK_OUTPUT_DIRECTORY + "\\" + name;
        String stackDir = outputDir + "\\stacks\\" + name;

        App app = App.Builder.create()
                .outdir(outputDir)
                .build();

        // Create stack
        log.warn("Creating stack");

        HashMap<CompositeKey, String> terraformOutputNames = createStack(name, app, associatedTest);
        log.warn("Finished creating");

        // Synthesize it
        log.warn("Synthesizing stack");

        app.synth();
        log.warn("Finished synthesizing");

        // Deploy
        log.warn("Deploying stack");

        deployStack(stackDir);
        log.warn("Finished deploying");

        // Get functionUrls
        log.warn("Getting function URLs");
        HashMap<CompositeKey, String> functionUrls = getFunctionUrls(stackDir, terraformOutputNames);

        return CompletableFuture.completedFuture(functionUrls);
    }



    private HashMap<CompositeKey, String> createStack(@NotBlank String name,
                                                      @NotNull App app,
                                                      @NotNull Test associatedTest) {

        MainStack mainStack = new MainStack(app, name);

        HashMap<CompositeKey, String> outputNamesAndUrlsMap = new HashMap<>();

        for (ProviderAccount account : associatedTest.getAccountsList()) {

            for (ResourceConfiguration configuration : associatedTest.getResourceConfigurations()) {

                if (account.getAccountType().equals("AwsAccount") &&
                        configuration.getProviderPlatform().equals(Platform.AWS)) {

                    assert account instanceof AwsAccount;
                    AwsAccount awsAccount = (AwsAccount) account;

                    AwsConstruct newAwsConstruct = mainStack.createAwsConstruct(
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

                    addOutputNamesToMap(outputNamesAndUrlsMap,
                            newAwsConstruct.getTerraformOutputsList(), configuration, account);
                }

                if (account.getAccountType().equals("GcpAccount") &&
                        configuration.getProviderPlatform().equals(Platform.GCP)) {

                    assert account instanceof GcpAccount;
                    GcpAccount gcpAccount = (GcpAccount) account;

                    GcpConstruct newGcpConstruct = mainStack.createGcpConstruct(
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

                    addOutputNamesToMap(outputNamesAndUrlsMap,
                            newGcpConstruct.getTerraformOutputsList(), configuration, account);
                }
            }
        }

        return outputNamesAndUrlsMap;
    }


    private void deployStack(@NotBlank String stackDir) {

        File terraformDirectory = new File(stackDir + "\\.terraform");
        File stackDirectory = new File(stackDir);

        // Initialize terraform
        if (!terraformDirectory.exists()) {

            createProcess(stackDirectory, TerraformCommand.INIT.get());
        }

        try {

            // Deploy infrastructure
            createProcess(stackDirectory, TerraformCommand.APPLY.get());
        }
        catch (RuntimeException ex) {

            deleteStack(stackDir);
            throw new StackDeploymentException(ex.getMessage() + "\nStack was deleted\n");
        }


    }


    public void deleteStack(@NotBlank String stackName, @NotBlank String deploymentId) {

        String name = stackName + "-" + deploymentId;
        String outputDir = STACK_OUTPUT_DIRECTORY + "\\" + name;
        String stackDir = outputDir + "\\stacks\\" + name;

        File stackDirectory = new File(stackDir);

        createProcess(stackDirectory, TerraformCommand.DESTROY.get());
    }

    public void deleteStack(@NotBlank String stackDir) {

        File stackDirectory = new File(stackDir);

        createProcess(stackDirectory, TerraformCommand.DESTROY.get());
    }

    private HashMap<CompositeKey, String> getFunctionUrls(@NotBlank String stackDirectory,
                                                          @NotNull HashMap<CompositeKey, String> terraformOutputNames) {

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
            return extractUrls(terraformOutputNames, rawTerraformOutputs.toString());
        }
        catch (IOException ex) {
            log.error(ex.getMessage());
        }


        File errorFile = new File(processBuilder.directory().getPath() + "\\output-errors.txt");
        processBuilder.redirectError(errorFile);

        throw new StackDeploymentException(errorFile, TerraformCommand.OUTPUT.get());
    }



    private HashMap<CompositeKey, String> extractUrls(@NotNull HashMap<CompositeKey, String> urlMap,
                                                      @NotBlank String functionUrls) {

        String[] lines = functionUrls.split("\\n");

        for (String line : lines) {

            String[] parts = line.split("=");
            String outputName = parts[0].trim();
            String functionUrl = parts[1].trim().replace("\"", "");

            for (Map.Entry<CompositeKey, String> entry : urlMap.entrySet()) {

                // Using contains instead of equals since terraform appends a unique id
                // in the final output name
                if(outputName.contains(entry.getKey().outputName())) {

                    urlMap.put(entry.getKey(), functionUrl);
                }

            }

        }

        return urlMap;
    }


    private void createProcess(@NotNull File stackDirectory, @NotNull String ... commands) {

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(stackDirectory);
        processBuilder.command(commands);

        String commandString = "\\" + String.join(" ", commands);
        commandString = commandString.replace(" ", "_").replace("-", "_");

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


    private void addOutputNamesToMap(@NotNull HashMap<CompositeKey, String> outputNamesAndUrlsMap,
                                     @NotNull List<String> terraformOutputsList,
                                     @NotNull ResourceConfiguration configuration,
                                     @NotNull ProviderAccount accountUsed) {

        for (String outputName : terraformOutputsList) {
            CompositeKey key = new CompositeKey(outputName, configuration, accountUsed);
            outputNamesAndUrlsMap.put(key, null);
        }
    }


}
