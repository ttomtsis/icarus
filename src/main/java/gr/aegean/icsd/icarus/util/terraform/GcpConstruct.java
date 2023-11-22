package gr.aegean.icsd.icarus.util.terraform;

import com.hashicorp.cdktf.ITerraformDependable;
import com.hashicorp.cdktf.TerraformOutput;
import com.hashicorp.cdktf.providers.google.cloud_run_service_iam_member.CloudRunServiceIamMember;
import com.hashicorp.cdktf.providers.google.cloudfunctions2_function.*;
import com.hashicorp.cdktf.providers.google.provider.GoogleProvider;
import com.hashicorp.cdktf.providers.google.storage_bucket.StorageBucket;
import com.hashicorp.cdktf.providers.google.storage_bucket_object.StorageBucketObject;
import gr.aegean.icsd.icarus.util.enums.gcp.GcfRuntime;
import gr.aegean.icsd.icarus.util.enums.gcp.GcpRegion;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class GcpConstruct extends Construct
{

    private final String GUID;

    private final String functionName;
    private final String functionDescription;
    private final String functionSource;
    private final GcfRuntime functionRuntime;
    private final String functionEntrypoint;

    private final String credentials;
    private final String project;

    private final Set<Integer> memoryConfigurations;
    private final Set<Integer> cpuConfigurations;
    private Set<String> locations;

    ArrayList<ITerraformDependable> dependencies = new ArrayList<>();


    public GcpConstruct(final Construct scope, final String id,
                        String gcfCredentials, String gcfFunctionSource,
                        String gcfFunctionName, String gcfFunctionDescription,
                        String gcpProject, GcfRuntime gcfRuntime, String gcfFunctionEntrypoint,
                        Set<Integer> memoryConfigs, Set<Integer> cpuConfigs, Set<GcpRegion> regions) {

        super(scope, id);

        this.GUID = UUID.randomUUID().toString().substring(0, 8);
        this.credentials = gcfCredentials;
        this.project = gcpProject;

        this.functionSource = gcfFunctionSource;
        this.functionRuntime = gcfRuntime;
        this.functionEntrypoint = gcfFunctionEntrypoint;
        this.functionDescription = gcfFunctionDescription;
        this.functionName = gcfFunctionName;

        this.memoryConfigurations = memoryConfigs;
        this.cpuConfigurations = cpuConfigs;

        for (GcpRegion region : regions) {
            this.locations.add(region.get());
        }


        GoogleProvider.Builder.create(this, "google-" + GUID)
                .project(project)
                .credentials(credentials)
                .build();


        for (String location: locations) {

            String bucketName = "bucket-" + location + "-" + GUID;
            String objectName = "function_source-" + location + "-" + GUID;
            StorageBucketObject object = createBucket(bucketName, objectName, functionSource, location);

            for (int memory: memoryConfigurations) {

                for (int cpu: cpuConfigurations) {

                    String name = functionName + "-" + memory + "mb-" + location + "-" + cpu + "vcpu" + "-" + GUID;
                    name = name.toLowerCase();

                    dependencies.clear();
                    dependencies.add(object);
                    Cloudfunctions2Function newFunction = createFunction(bucketName, objectName,
                            dependencies, name, location,
                            Integer.toString(memory), Integer.toString(cpu),
                            1, 60);

                    dependencies.clear();
                    dependencies.add(newFunction);
                    createServiceIAMmember(location, name);

                    createOutput("gcfURI-" + name, newFunction);

                }
            }
        }

    }


    private StorageBucketObject createBucket(String bucketName, String objectName,
                                             String objectLocation, String location) {

        StorageBucket bucket = StorageBucket.Builder.create(this, "bucket-" + location + "-" + GUID)
                .name(bucketName)
                .location(location)
                .uniformBucketLevelAccess(true)
                .build();

        dependencies.clear();
        dependencies.add(bucket);

        return StorageBucketObject.Builder.create(this, "object-" + location + "-" + GUID)
                .name(objectName)
                .bucket(bucketName)
                .source(objectLocation)
                .dependsOn(dependencies)
                .build();

    }

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
                                .runtime(functionRuntime.toString())
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

    private void createServiceIAMmember(String location, String name) {

        CloudRunServiceIamMember.Builder.create(this, "iam_member-" + name)
                .location(location)
                .service(name)
                .role("roles/run.invoker")
                .member("allUsers")
                .dependsOn(dependencies)
                .build();
    }

    private void createOutput(String id, Cloudfunctions2Function function) {

        TerraformOutput.Builder.create(this, id)
                .value(function.getServiceConfig().getUri())
                .build();
    }


}