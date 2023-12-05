package gr.aegean.icsd.icarus.util.aws;

public class AwsMetricRequest {

    private final String accessKey;
    private final String secretKey;

    private final String functionName;
    private final String metricName;


    public AwsMetricRequest(String awsAccessKey, String awsSecretKey, String lambdaFunctionName,
                            String cloudwatchMetricName) {

        this.accessKey = awsAccessKey;
        this.secretKey = awsSecretKey;

        this.functionName = lambdaFunctionName;
        this.metricName = cloudwatchMetricName;



    }

}
