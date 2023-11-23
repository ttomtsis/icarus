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

/**
 * Class used to model AWS Lambda Functions in the Terraform CDK as Constructs <br>
 * Used as part of a parent Stack.
 */
public class AwsConstruct extends Construct {

    /**
     * Unique 8-digit ID that identifies all resources deployed as
     * part of an AWS Construct. <br>
     * Every AWS Construct uses a different GUID
     */
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

    // Currently not supported due to restrictions on AWS starter accounts
    // private final int concurrentExecutions = 10;

    /**
     * Default constructor of the class, used to model an AWS Lambda function <br><br>
     *
     * This construct creates an S3 Bucket and Bucket object per region that all
     * Lambda functions in that region will use. <br><br>
     *
     * In addition to this, it creates a lambda function per requested memory configuration
     * and then matches that function with a new API Gateway deployed in a
     * separate Gateway Stage. <br>
     *
     * @param scope Scope of the construct <br>
     * @param id ID of the construct <br>
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

        // Default, empty provider. Used solely because Terraform requires a default provider.
        // This provider is never used.
        AwsProvider.Builder.create(this, "awsProvider")
                .region("ap-northeast-3")
                .accessKey(accessKey)
                .secretKey(secretKey)
                .build();

        Set<AwsProvider> providers = createProviders();

        // Create the IAM Role that functions will require
        IamRole functionRole = createIamRole();

        // Create a function, bucket and api gateway for every requested region
        for (AwsProvider provider : providers) {

            S3Bucket myBucket = createBucket(provider, provider.getRegion());
            S3Object bucketObject = createBucketObject(provider, myBucket, provider.getRegion());

            // Create a function and api gateway for every requested memory configuration
            for (Integer memory: memoryConfigurations) {

                String targetFunctionID = functionName + "-" + memory + "mb-" + provider.getRegion();

                LambdaFunction lambdaFunction = createFunction(myBucket, bucketObject,
                        functionRole, provider, memory, targetFunctionID);

                Apigatewayv2Stage stage = createAPIGateway(provider, lambdaFunction, targetFunctionID);

                // Create an output that will print the URL used to invoke the function
                TerraformOutput.Builder.create(
                    this, "lambda_url-" + functionName + "-" + provider.getRegion() + "-" + memory )
                    .description("Base URL for API Gateway stage.")
                    .value(stage.getInvokeUrl())
                    .build();
            }

        }

    }

    /**
     * Creates a list of AWS Providers according to the list of locations specified <br>
     * Creates one provider per location. <br>
     * These providers will be used to deploy resources to all desired locations
     *
     * @return Set of AwsProviders
     */
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

    /**
     * Creates an IamRole that all functions created by this construct will assume when executing
     *
     * @return IamRole that functions will use
     */
    private IamRole createIamRole() {

        DataAwsIamPolicyDocument policyDocument = createDocument();
        IamRole functionRole = IamRole.Builder.create(this, "lambda_exec-" + GUID)
                .name("serverless_lambda-" + GUID)
                .assumeRolePolicy(policyDocument.getJson())
                .build();

        IamRolePolicyAttachment.Builder.create(this, "lambda_policy-" + GUID)
            .role(functionRole.getName())
            .policyArn("arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole")
            .build();

        return functionRole;
    }

    /**
     * Creates an Iam Policy Document that specifies the privileges the Iam Role can use <br>
     *
     * @return An Iam Policy Document
     */
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

    /**
     * Creates a Bucket that will be used to store the function's source code
     *
     * @param provider Provider that will be used to create the Bucket
     * @param location Region where the bucket will be created
     *
     * @return S3 Bucket with proper ACL controls applied
     */
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

        S3BucketAcl.Builder.create(
            this, "lambda_bucket_acl-" + location + "-" + GUID)
            .bucket(myBucket.getId())
            .acl("private")
            .dependsOn(dependencies)
            .provider(provider)
            .build();

        return myBucket;

    }

    /**
     * Creates an S3 Object that will contain the function's source code
     *
     * @param provider Provider used to create the object
     * @param bucket S3 Bucket that will contain the object
     * @param location Location where the object will be created
     *
     * @return S3 Object containing the function's source code
     */
    private S3Object createBucketObject(AwsProvider provider, S3Bucket bucket, String location){

        return S3Object.Builder.create(this, "lambda_object-" + location + "-" + GUID)
                .bucket(bucket.getId())
                .key(functionArchiveName)
                .source(functionSource)
                .provider(provider)
                .build();
    }

    /**
     * Creates a Lambda function and associates a CloudWatch log group with it
     *
     * @param bucket Bucket used to store the Bucket Object containing the function's source code
     * @param bucketObject Bucket Object where the source code is stored
     * @param functionRole IAM Role that the function will assume when executing
     * @param provider Provider used to deploy the function
     * @param memory Memory configuration of the function
     * @param targetFunctionID The basic name of the function
     *
     * @return A Lambda Function
     */
    private LambdaFunction createFunction(S3Bucket bucket, S3Object bucketObject, IamRole functionRole,
                                          AwsProvider provider, int memory, String targetFunctionID) {

        LambdaFunction lambdaFunction = LambdaFunction.Builder.create(
                this, targetFunctionID + "-" + GUID)
                .functionName(targetFunctionID + "-" + GUID )
                .s3Bucket(bucket.getId())
                .s3Key(bucketObject.getKey())
                .runtime(functionRuntime)
                .handler(functionHandler)
                .memorySize(memory)
                .role(functionRole.getArn())
                //.reservedConcurrentExecutions(concurrentExecutions)
                .provider(provider)
                .build();

        CloudwatchLogGroup.Builder.create(
                    this, "log_group-" + targetFunctionID + "-" + GUID)
            .name("/aws/lambda/"+lambdaFunction.getFunctionName())
            .retentionInDays(30)
            .build();

        return lambdaFunction;

    }

    /**
     * Create an API Gateway, that will expose the function to the internet
     *
     * @param provider Provider used to create the API
     * @param lambdaFunction Function that will be exposed
     * @param targetFunctionID Basic name of the function that will be exposed
     *
     * @return The API Gateway Stage where the API Gateway is created
     */
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

        Apigatewayv2Route.Builder.create(this, "route-" + apiName)
            .apiId(api.getId())
            .routeKey("$default")
            .target("integrations/"+integration.getId())
            .provider(provider)
            .build();

        LambdaPermission.Builder.create(this,
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
