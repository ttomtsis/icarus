package gr.aegean.icsd.icarus.function;

import com.hashicorp.cdktf.App;
import gr.aegean.icsd.icarus.provideraccount.AwsAccount;
import gr.aegean.icsd.icarus.provideraccount.GcpAccount;
import gr.aegean.icsd.icarus.provideraccount.ProviderAccount;
import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.test.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.util.aws.AwsRegion;
import gr.aegean.icsd.icarus.util.aws.LambdaRuntime;
import gr.aegean.icsd.icarus.util.enums.Platform;
import gr.aegean.icsd.icarus.util.exceptions.FunctionNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.TestNotFoundException;
import gr.aegean.icsd.icarus.util.gcp.GcfRuntime;
import gr.aegean.icsd.icarus.util.gcp.GcpRegion;
import gr.aegean.icsd.icarus.util.terraform.MainStack;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static gr.aegean.icsd.icarus.util.configuration.TerraformConfiguration.STACK_OUTPUT_DIRECTORY;


@Service
public class FunctionService {


    private final TestRepository testRepository;
    private final FunctionRepository functionRepository;



    public FunctionService(TestRepository testRepository, FunctionRepository repository) {
        this.testRepository = testRepository;
        this.functionRepository = repository;
    }



    public Function createFunction(@NotNull Function newFunction, @NotNull @Positive Long testId) {

        // TODO: See issue #1 at GitHub
        Test associatedTest = checkIfTestExists(testId);
        Function savedFunction = functionRepository.save(newFunction);

        associatedTest.setTargetFunction(savedFunction);
        testRepository.save(associatedTest);

        return savedFunction;
    }

    public void deleteFunction(@NotNull @Positive Long testId, @NotNull @Positive Long functionId) {

        checkIfTestExists(testId);

        Function existingFunction = checkIfFunctionExists(functionId);

        functionRepository.delete(existingFunction);
    }

    public void updateFunction(@NotNull @Positive Long testId, @NotNull @Positive Long functionId,
                                @NotNull FunctionModel model) {

        checkIfTestExists(testId);

        Function existingFunction = checkIfFunctionExists(functionId);

        setIfNotBlank(existingFunction::setName, model.getName());
        setIfNotBlank(existingFunction::setDescription, model.getDescription());
        setIfNotBlank(existingFunction::setGithubURL, model.getGithubURL());
        setIfNotBlank(existingFunction::setFunctionSourceDirectory, model.getFunctionSourceDirectory());
        setIfNotBlank(existingFunction::setFunctionSourceFileName, model.getFunctionSourceFileName());
        setIfNotBlank(existingFunction::setFunctionHandler, model.getFunctionHandler());

        functionRepository.save(existingFunction);
    }

    private void setIfNotBlank(Consumer<String> setter, String value) {

        if (StringUtils.isNotBlank(value)) {
            setter.accept(value);
        }
    }

    public Function getFunction(@NotNull @Positive Long testId, @NotNull @Positive Long functionId) {

        checkIfTestExists(testId);

        return functionRepository.findById(functionId)
                .orElseThrow(() -> new FunctionNotFoundException(functionId));
    }


    private Test checkIfTestExists(Long associatedTestId) {

        return testRepository.findById(associatedTestId)
                .orElseThrow( () -> new TestNotFoundException(associatedTestId));
    }

    private Function checkIfFunctionExists(Long functionId) {

        return functionRepository.findById(functionId)
                .orElseThrow( () -> new FunctionNotFoundException(functionId));
    }


    @Async
    public void deployFunction(@NotNull Test associatedTest) {

        String name = associatedTest.getTargetFunction().getName() + UUID.randomUUID().toString().substring(0, 5);
        String outputDir = STACK_OUTPUT_DIRECTORY + "/" + name;

        App app = App.Builder.create()
                .outdir(outputDir)
                .build();

        MainStack mainStack = new MainStack(app, name);

        for (ProviderAccount account : associatedTest.getAccountsList()) {

            for (ResourceConfiguration configuration : associatedTest.getResourceConfigurations()) {

                if (configuration.getProviderPlatform().equals(Platform.AWS)) {

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

        app.synth();

    }


}
