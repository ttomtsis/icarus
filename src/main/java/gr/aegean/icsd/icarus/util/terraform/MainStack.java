package gr.aegean.icsd.icarus.util.terraform;

import com.hashicorp.cdktf.TerraformStack;
import gr.aegean.icsd.icarus.util.enums.aws.AwsRegion;
import gr.aegean.icsd.icarus.util.enums.aws.LambdaRuntime;
import gr.aegean.icsd.icarus.util.enums.gcp.GcfRuntime;
import gr.aegean.icsd.icarus.util.enums.gcp.GcpRegion;
import software.constructs.Construct;

import java.util.Set;

/**
 * Parent Stack, used in conjunction with constructs in order to deploy functions from
 * multiple providers
 */
public class MainStack extends TerraformStack {


    public MainStack(final Construct scope, final String id) {

        super(scope, id);

    }


    /**
     * Creates an AWS construct in the scope of this Stack, used to model an AWS Lambda function <br><br>
     *
     * @param awsAccessKey Access key of the account used to deploy the function <br>
     * @param awsSecretKey Secret key of the account used to deploy the function <br>
     *
     * @param objectSource Location of the function's source code <br>
     * @param objectFileName Name of the zip archive containing the function's source code <br>
     *
     * @param awsRegions List of regions where the function will be deployed <br>
     * @param memoryConfs List of memory configurations according to which the function will be deployed <br>
     *
     * @param awsFunctionName Name of the function, this is used in a variety of supporting
     *                        resources as well as the function <br>
     * @param awsFunctionRuntime Runtime that will be used to execute the function <br>
     * @param awsFunctionHandler The appropriate handler that AWS Lambda will use to execute the function <br>
     */
    public void createAwsConstruct(String awsAccessKey, String awsSecretKey,
                                   String objectSource, String objectFileName,
                                   Set<AwsRegion> awsRegions, Set<Integer> memoryConfs,
                                   String awsFunctionName, LambdaRuntime awsFunctionRuntime,
                                   String awsFunctionHandler) {

        new AwsConstruct(this, "awsConstruct",
                awsAccessKey, awsSecretKey,
                objectSource, objectFileName,
                awsRegions, memoryConfs,
                awsFunctionName, awsFunctionRuntime, awsFunctionHandler);

    }

    /**
     * Creates a GCP construct in the scope of this Stack, used to model a GCF function <br><br>
     *
     * @param gcpCredentials GCP keyfile.json file in String format <br>
     *
     * @param gcfFunctionSource GCF function source code file <br>
     * @param gcfFunctionName GCF function name <br>
     * @param gcfFunctionDescription GCF function description <br>
     *
     * @param gcpProject GCP project where the resources will be deployed <br>
     * @param gcfRuntime GCF function runtime <br>
     * @param gcfFunctionEntrypoint GCF function entrypoint <br>
     *
     * @param memoryConfigs Memory configurations for the GCF function <br>
     * @param cpuConfigs CPU configurations for the GCF function <br>
     * @param regions Regions where the GCF function will be deployed <br>
     */
    public void createGcpConstruct(String gcpCredentials, String gcfFunctionSource,
                                   String gcfFunctionName, String gcfFunctionDescription,
                                   String gcpProject, GcfRuntime gcfRuntime, String gcfFunctionEntrypoint,
                                   Set<Integer> memoryConfigs, Set<Integer> cpuConfigs, Set<GcpRegion> regions) {

        new GcpConstruct(this, "gcpConstruct",
                gcpCredentials, gcfFunctionSource,
                gcfFunctionName, gcfFunctionDescription,
                gcpProject, gcfRuntime, gcfFunctionEntrypoint,
                memoryConfigs, cpuConfigs, regions);

    }

}