package gr.aegean.icsd.icarus.util.terraform;

import com.hashicorp.cdktf.ITerraformDependable;
import com.hashicorp.cdktf.TerraformOutput;
import com.hashicorp.cdktf.providers.google.cloud_run_service_iam_member.CloudRunServiceIamMember;
import com.hashicorp.cdktf.providers.google.cloudfunctions2_function.*;
import com.hashicorp.cdktf.providers.google.provider.GoogleProvider;
import com.hashicorp.cdktf.providers.google.storage_bucket.StorageBucket;
import com.hashicorp.cdktf.providers.google.storage_bucket_object.StorageBucketObject;
import gr.aegean.icsd.icarus.util.enums.Platform;
import gr.aegean.icsd.icarus.util.gcp.GcfRuntime;
import gr.aegean.icsd.icarus.util.gcp.GcpRegion;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Class used to model GCF Functions in the Terraform CDK as Constructs <br>
 * Used as part of a parent Stack.
 */
public class GcpConstruct extends Construct
{
    /**
     * Unique 8-digit ID that identifies all resources deployed as
     * part of a GCP Construct. <br>
     * Every GCP Construct uses a different deploymentId
     */
    private final String deploymentId;
    private final String guid = UUID.randomUUID().toString().substring(0, 8);


    private final String functionDescription;
    private final GcfRuntime functionRuntime;
    private final String functionEntrypoint;

    ArrayList<ITerraformDependable> dependencies = new ArrayList<>();


    private final Set<DeploymentRecord> deploymentRecords = new HashSet<>();



    /**
     * Default constructor of the class, used to model a GCF function <br><br>
     * Uses an existing GCP Project to deploy a Bucket, per desired region,
     * which functions are going to use. <br>
     * Deploys a function per memory and cpu configuration
     * to every desired region
     *
     * @param scope Scope of the Construct <br>
     * @param id ID of the Construct <br>
     * @param deploymentId ID of deployment
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
    public GcpConstruct(final Construct scope, final String id, String deploymentId,
                        String gcpCredentials, String gcfFunctionSource, String gcfFunctionSourceFileName,
                        String gcfFunctionName, String gcfFunctionDescription,
                        String gcpProject, GcfRuntime gcfRuntime, String gcfFunctionEntrypoint,
                        Set<Integer> memoryConfigs, Set<Integer> cpuConfigs, Set<GcpRegion> regions) {

        super(scope, id);

        this.deploymentId = deploymentId;

        String functionSource = gcfFunctionSource + "/" + gcfFunctionSourceFileName;
        this.functionRuntime = gcfRuntime;
        this.functionEntrypoint = gcfFunctionEntrypoint;
        this.functionDescription = gcfFunctionDescription;

        Set<Integer> cpuConfigurations = cpuConfigs;

        Set<String> locations = new HashSet<>();
        for (GcpRegion region : regions) {
            locations.add(region.get());
        }


        GoogleProvider.Builder.create(this, "google-" + this.deploymentId + "-" + guid)
                .project(gcpProject)
                .credentials(gcpCredentials)
                .build();

        if (cpuConfigurations == null || cpuConfigurations.isEmpty()) {
            cpuConfigurations = new HashSet<>();
            cpuConfigurations.add(1);
        }

        for (String location: locations) {

            String bucketName = "bucket-" + location + "-" + this.deploymentId+ "-" + guid;
            String objectName = "function_source-" + location + "-" + this.deploymentId+ "-" + guid;
            StorageBucketObject object = createBucket(bucketName, objectName, functionSource, location);

            for (int memory: memoryConfigs) {

                for (int cpu: cpuConfigurations) {

                    String name = gcfFunctionName + "-" + memory + "mb-" + location + "-" + cpu + "vcpu" + "-" + this.deploymentId;
                    name = name.toLowerCase();

                    DeploymentRecord newRecord = new DeploymentRecord(name, location, memory, this.deploymentId, Platform.GCP);
                    newRecord.deployedCpu = cpu;

                    deploymentRecords.add(newRecord);

                    String functionMemory = memory + "M";

                    dependencies.clear();
                    dependencies.add(object);
                    Cloudfunctions2Function newFunction = createFunction(bucketName, objectName,
                            dependencies, name, location,
                            functionMemory, Integer.toString(cpu),
                            1, 60);

                    dependencies.clear();
                    dependencies.add(newFunction);
                    createServiceIamMember(location, name);

                    createOutput("gcf_url_" + name, newFunction);

                }
            }
        }

    }


    /**
     * Create a Storage Bucket that will contain the function's source code as an object
     *
     * @param bucketName Name of the Bucket
     * @param objectName Name of the Bucket's Object
     * @param objectLocation Location of the function's source code
     * @param location Region where the bucket will be deployed
     *
     * @return A Storage Bucket Object containing the function's source code
     */
    private StorageBucketObject createBucket(String bucketName, String objectName,
                                             String objectLocation, String location) {

        StorageBucket bucket = StorageBucket.Builder.create(this, "bucket-" + location + "-" + deploymentId)
                .name(bucketName)
                .location(location)
                .uniformBucketLevelAccess(true)
                .build();

        dependencies.clear();
        dependencies.add(bucket);

        return StorageBucketObject.Builder.create(this, "object-" + location + "-" + deploymentId)
                .name(objectName)
                .bucket(bucketName)
                .source(objectLocation)
                .dependsOn(dependencies)
                .build();

    }

    /**
     * Create a GCF Function
     *
     * @param bucketName Name of the Bucket where the object with the function's source code is located
     * @param objectName Name of the Object that contains the function's source code
     * @param dependencies Required dependencies prior to the function's creation
     * @param name Name of the function
     * @param location GCP region where the function will be deployed
     * @param memory Memory configuration that the function will use
     * @param cpu CPU configuration that the function will use
     * @param instances Number of Instances the function is allowed to use, defaults to 1
     * @param timeout Maximum amount of time the function may execute ( in seconds ), defaults to 60
     *
     * @return A fully configured GCF v2 function
     */
    private Cloudfunctions2Function createFunction(String bucketName, String objectName,
                                                   ArrayList<ITerraformDependable> dependencies,
                                                   String name, String location,
                                                   String memory, String cpu, int instances,
                                                   int timeout) {

        return Cloudfunctions2Function.Builder.create(this, name)
                .name(name)
                .location(location)
                .description(functionDescription)
                .buildConfig(
                        Cloudfunctions2FunctionBuildConfig.builder()
                                .runtime(functionRuntime.get())
                                .entryPoint(functionEntrypoint)
                                .source(
                                        Cloudfunctions2FunctionBuildConfigSource.builder()
                                                .storageSource(
                                                        Cloudfunctions2FunctionBuildConfigSourceStorageSource.builder()
                                                                .bucket(bucketName)
                                                                .object(objectName)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .serviceConfig(
                        Cloudfunctions2FunctionServiceConfig.builder()
                                .maxInstanceCount(instances)
                                .timeoutSeconds(timeout)
                                .availableMemory(memory)
                                .availableCpu(cpu)
                                .build()
                )
                .dependsOn(dependencies)
                .build();
    }

    /**
     * Create an IAM member that functions will assume when executed
     *
     * @param location Region where the member will be created
     * @param name Name of the service that will assume this member
     */
    private void createServiceIamMember(String location, String name) {

        CloudRunServiceIamMember.Builder.create(this, "iam_member-" + name)
                .location(location)
                .service(name)
                .role("roles/run.invoker")
                .member("allUsers")
                .dependsOn(dependencies)
                .build();
    }

    /**
     * Creates a terraform output that will print the invocation url of a function
     *
     * @param id ID of the resource in the context of this construct
     * @param function Name of the function
     */
    private void createOutput(String id, Cloudfunctions2Function function) {

        TerraformOutput.Builder.create(this, id)
                .value(function.getServiceConfig().getUri())
                .build();
    }


    public Set<DeploymentRecord> getDeploymentRecords() {
        return this.deploymentRecords;
    }


}