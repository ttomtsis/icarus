package gr.aegean.icsd.icarus.util.terraform;

import com.hashicorp.cdktf.ITerraformDependable;
import com.hashicorp.cdktf.TerraformOutput;
import com.hashicorp.cdktf.providers.aws.apigatewayv2_api.Apigatewayv2Api;
import com.hashicorp.cdktf.providers.aws.apigatewayv2_integration.Apigatewayv2Integration;
import com.hashicorp.cdktf.providers.aws.apigatewayv2_route.Apigatewayv2Route;
import com.hashicorp.cdktf.providers.aws.apigatewayv2_stage.Apigatewayv2Stage;
import com.hashicorp.cdktf.providers.aws.apigatewayv2_stage.Apigatewayv2StageAccessLogSettings;
import com.hashicorp.cdktf.providers.aws.cloudwatch_log_group.CloudwatchLogGroup;
import com.hashicorp.cdktf.providers.aws.data_aws_iam_policy_document.DataAwsIamPolicyDocument;
import com.hashicorp.cdktf.providers.aws.data_aws_iam_policy_document.DataAwsIamPolicyDocumentStatement;
import com.hashicorp.cdktf.providers.aws.data_aws_iam_policy_document.DataAwsIamPolicyDocumentStatementPrincipals;
import com.hashicorp.cdktf.providers.aws.iam_role.IamRole;
import com.hashicorp.cdktf.providers.aws.iam_role_policy_attachment.IamRolePolicyAttachment;
import com.hashicorp.cdktf.providers.aws.lambda_function.LambdaFunction;
import com.hashicorp.cdktf.providers.aws.lambda_permission.LambdaPermission;
import com.hashicorp.cdktf.providers.aws.provider.AwsProvider;
import com.hashicorp.cdktf.providers.aws.s3_bucket.S3Bucket;
import com.hashicorp.cdktf.providers.aws.s3_bucket_acl.S3BucketAcl;
import com.hashicorp.cdktf.providers.aws.s3_bucket_ownership_controls.S3BucketOwnershipControls;
import com.hashicorp.cdktf.providers.aws.s3_bucket_ownership_controls.S3BucketOwnershipControlsRule;
import com.hashicorp.cdktf.providers.aws.s3_object.S3Object;
import gr.aegean.icsd.icarus.util.enums.aws.AwsRegion;
import gr.aegean.icsd.icarus.util.enums.aws.LambdaRuntime;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AwsConstruct extends Construct {

    private final String GUID;

    private final Set<String> locations = new HashSet<>();
    private final Set<Integer> memoryConfigurations;

    private final String accessKey;
    private final String secretKey;
    private final String functionSource;
    private final String functionArchiveName;


    private final String functionName;
    private final String functionRuntime;
    private final String functionHandler;

    private final int concurrentExecutions = 10;

    public AwsConstruct(final Construct scope, final String id,
                        String awsAccessKey, String awsSecretKey,
                        String objectSource, String objectFileName,
                        Set<AwsRegion> awsRegions, Set<Integer> memoryConfs,
                        String awsFunctionName, LambdaRuntime awsFunctionRuntime, String awsFunctionHandler) {

        super(scope, id);

        this.GUID = UUID.randomUUID().toString().substring(0, 8);

        this.accessKey = awsAccessKey;
        this.secretKey = awsSecretKey;

        this.functionSource = objectSource;
        this.functionArchiveName = objectFileName;

        this.memoryConfigurations = memoryConfs;

        for ( AwsRegion region : awsRegions ) {
            locations.add(region.get());
        }

        this.functionName = awsFunctionName;
        this.functionRuntime = awsFunctionRuntime.get();
        this.functionHandler = awsFunctionHandler;

        // Default, empty provider
        AwsProvider.Builder.create(this, "awsProvider")
                .region("ap-northeast-3")
                .accessKey(accessKey)
                .secretKey(secretKey)
                .build();

        Set<AwsProvider> providers = createProviders();

        IamRole functionRole = createIamRole();

        for (AwsProvider provider : providers) {

            S3Bucket myBucket = createBucket(provider, provider.getRegion());
            S3Object bucketObject = createBucketObject(provider, myBucket, provider.getRegion());

            for (Integer memory: memoryConfigurations) {

                String targetFunctionID = functionName + "-" + memory + "mb-" + provider.getRegion();

                LambdaFunction lambdaFunction = createFunction(myBucket, bucketObject,
                        functionRole, provider, memory, targetFunctionID);

                Apigatewayv2Stage stage = createAPIGateway(provider, lambdaFunction, targetFunctionID);

                TerraformOutput baseUrl = TerraformOutput.Builder.create(
                        this, "lambda_url-" + functionName + "-" + provider.getRegion() + "-" + memory )
                        .description("Base URL for API Gateway stage.")
                        .value(stage.getInvokeUrl())
                        .build();
            }

        }

    }

    private Set<AwsProvider> createProviders() {

        Set<AwsProvider> providers = new HashSet<>();

        for (String location : locations) {
            providers.add(
                    AwsProvider.Builder.create(this, "awsProvider-"+location)
                            .region(location)
                            .accessKey(accessKey)
                            .secretKey(secretKey)
                            .alias("aws-" + location)
                            .build()
            );
        }

        return providers;
    }

    private IamRole createIamRole() {

        DataAwsIamPolicyDocument policyDocument = createDocument();
        IamRole functionRole = IamRole.Builder.create(this, "lambda_exec-" + GUID)
                .name("serverless_lambda-" + GUID)
                .assumeRolePolicy(policyDocument.getJson())
                .build();

        IamRolePolicyAttachment functionAttachment = IamRolePolicyAttachment.Builder.create(this, "lambda_policy-" + GUID)
                .role(functionRole.getName())
                .policyArn("arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole")
                .build();

        return functionRole;
    }

    private DataAwsIamPolicyDocument createDocument() {

        // Principals
        ArrayList<String> identifiers = new ArrayList<>();
        identifiers.add("lambda.amazonaws.com");

        ArrayList<DataAwsIamPolicyDocumentStatementPrincipals> principals = new ArrayList<>();
        principals.add(
                DataAwsIamPolicyDocumentStatementPrincipals.builder()
                        .type("Service")
                        .identifiers(identifiers)
                        .build()
        );

        // Statements
        ArrayList<String> actions = new ArrayList<>();
        actions.add("sts:AssumeRole");

        ArrayList<DataAwsIamPolicyDocumentStatement> statements = new ArrayList<>();
        statements.add(
                DataAwsIamPolicyDocumentStatement.builder()
                        .actions(actions)
                        .effect("Allow")
                        .sid("")
                        .principals(principals)
                        .build()
        );

        // Document
        return DataAwsIamPolicyDocument.Builder.create(this, "policy_document-" + GUID)
                .version("2012-10-17")
                .statement(statements)
                .build();
    }

    private S3Bucket createBucket(AwsProvider provider, String location) {

        S3Bucket myBucket = S3Bucket.Builder.create(this, "lambda_bucket-" + location + GUID)
                .bucket("lambda-bucket-" + location + "-" + GUID)
                .provider(provider)
                .build();


        S3BucketOwnershipControls bucketControls = S3BucketOwnershipControls.Builder.create(
                this, "lambda_bucket_controls-" + location + "-" + GUID)
                .bucket(myBucket.getId())
                .rule(
                        S3BucketOwnershipControlsRule.builder()
                                .objectOwnership("BucketOwnerPreferred")
                                .build()
                )
                .provider(provider)
                .build();

        ArrayList<ITerraformDependable> dependencies = new ArrayList<>();
        dependencies.add(myBucket);
        dependencies.add(bucketControls);

        S3BucketAcl bucketACL = S3BucketAcl.Builder.create(
                this, "lambda_bucket_acl-" + location + "-" + GUID)
                .bucket(myBucket.getId())
                .acl("private")
                .dependsOn(dependencies)
                .provider(provider)
                .build();

        return myBucket;

    }

    private S3Object createBucketObject(AwsProvider provider, S3Bucket bucket, String location){

        return S3Object.Builder.create(this, "lambda_object-" + location + "-" + GUID)
                .bucket(bucket.getId())
                .key(functionArchiveName)
                .source(functionSource)
                .provider(provider)
                .build();
    }

    private LambdaFunction createFunction(S3Bucket myBucket, S3Object bucketObject, IamRole functionRole,
                                          AwsProvider provider, int memory, String targetFunctionID) {

        LambdaFunction lambdaFunction = LambdaFunction.Builder.create(
                this, targetFunctionID + "-" + GUID)
                .functionName(targetFunctionID + "-" + GUID )
                .s3Bucket(myBucket.getId())
                .s3Key(bucketObject.getKey())
                .runtime(functionRuntime)
                .handler(functionHandler)
                .memorySize(memory)
                .role(functionRole.getArn())
                //.reservedConcurrentExecutions(concurrentExecutions)
                .provider(provider)
                .build();

        CloudwatchLogGroup functionLogGroup = CloudwatchLogGroup.Builder.create(
                        this, "log_group-" + targetFunctionID + "-" + GUID)
                .name("/aws/lambda/"+lambdaFunction.getFunctionName())
                .retentionInDays(30)
                .build();

        return lambdaFunction;

    }

    private Apigatewayv2Stage createAPIGateway(AwsProvider provider, LambdaFunction lambdaFunction,
                                               String targetFunctionID) {

        String apiName = "lambda_api-" + targetFunctionID + "-" + GUID;

        Apigatewayv2Api api = Apigatewayv2Api.Builder.create(this, apiName)
                .name("serverless_lambda_gw-" + GUID + "-" + lambdaFunction.getFunctionName())
                .protocolType("HTTP")
                .provider(provider)
                .build();

        CloudwatchLogGroup apiLogGroup = CloudwatchLogGroup.Builder.create(
                this, "api_log_group-" + apiName)
                .name("/aws/apigateway/" + api.getName())
                .retentionInDays(30)
                .provider(provider)
                .build();

        String accessLogFormat = "{\"requestId\":\"$context.requestId\",\"sourceIp\":\"$context.identity.sourceIp\",\"requestTime\":\"$context.requestTime\",\"protocol\":\"$context.protocol\",\"httpMethod\":\"$context.httpMethod\",\"resourcePath\":\"$context.resourcePath\",\"routeKey\":\"$context.routeKey\",\"status\":\"$context.status\",\"responseLength\":\"$context.responseLength\",\"integrationErrorMessage\":\"$context.integrationErrorMessage\"}";

        Apigatewayv2Stage stage = Apigatewayv2Stage.Builder.create(this, "stage-" + apiName)
                .apiId(api.getId())
                .name("serverless_lambda_stage-" + api.getName())
                .autoDeploy(true)
                .accessLogSettings(
                        Apigatewayv2StageAccessLogSettings.builder()
                                .destinationArn(apiLogGroup.getArn())
                                .format(accessLogFormat)
                                .build()
                )
                .provider(provider)
                .build();

        Apigatewayv2Integration integration = Apigatewayv2Integration.Builder.create(
                this, "integration-" + apiName)
                .apiId(api.getId())
                .integrationUri(lambdaFunction.getArn())
                .integrationType("AWS_PROXY")
                .integrationMethod("POST")
                .provider(provider)
                .build();

        Apigatewayv2Route route = Apigatewayv2Route.Builder.create(this, "route-" + apiName)
                .apiId(api.getId())
                .routeKey("$default")
                .target("integrations/"+integration.getId())
                .provider(provider)
                .build();

        LambdaPermission permission = LambdaPermission.Builder.create(this,
                        "permission-" + targetFunctionID + "-" + GUID)
                .statementId("AllowExecutionFromAPIGateway")
                .action("lambda:InvokeFunction")
                .functionName(lambdaFunction.getFunctionName())
                .principal("apigateway.amazonaws.com")
                .sourceArn(api.getExecutionArn()+"/*/*")
                .provider(provider)
                .build();

        return stage;
    }

}
