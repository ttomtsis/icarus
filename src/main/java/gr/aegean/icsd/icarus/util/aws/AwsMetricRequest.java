package gr.aegean.icsd.icarus.util.aws;

import gr.aegean.icsd.icarus.util.enums.Metric;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class AwsMetricRequest {


    private final String accessKey;
    private final String secretKey;

    private final String functionName;
    private final String functionRegion;

    private final String metricName;
    private final String metricNamespace;

    private final HashMap<String, String> metricResults = new HashMap<>();

    private final CloudWatchClient client;
    private final GetMetricDataRequest request;

    public AwsMetricRequest(String awsAccessKey, String awsSecretKey, String lambdaFunctionName,
                            AwsRegion functionRegion, Metric cloudwatchMetric) {

        this.accessKey = awsAccessKey;
        this.secretKey = awsSecretKey;

        this.functionName = lambdaFunctionName;
        this.functionRegion = functionRegion.get();

        this.metricName = cloudwatchMetric.getAwsMetricName();
        this.metricNamespace = cloudwatchMetric.getAwsNamespace();

        this.client = buildCloudwatchClient();
        this.request = createMetricRequest();

    }



    public void sendRequest() {

        getMetricDataResults(client, request);
    }


    private CloudWatchClient buildCloudwatchClient() {

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return CloudWatchClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of(functionRegion))
                .build();
    }

    private GetMetricDataRequest createMetricRequest() {

        return GetMetricDataRequest.builder()
                .startTime(Instant.now().minus(Duration.ofMinutes(60))) // 1 hour ago
                .endTime(Instant.now())
                .metricDataQueries(Collections.singletonList(
                        MetricDataQuery.builder()
                                .id("m1")
                                .metricStat(MetricStat.builder()
                                        .metric(software.amazon.awssdk.services.cloudwatch.model.Metric.builder()
                                                .namespace(this.metricNamespace)
                                                .metricName(this.metricName)
                                                .dimensions(Dimension.builder()
                                                        .name("FunctionName")
                                                        .value(this.functionName)
                                                        .build()
                                                )
                                                .build()
                                        )
                                        .period(60)
                                        .stat("Sum")
                                        .build()
                                )
                                .returnData(true)
                                .build()))
                .build();
    }

    private void getMetricDataResults(CloudWatchClient client, GetMetricDataRequest request) {

        GetMetricDataResponse response = client.getMetricData(request);

        for (MetricDataResult result : response.metricDataResults()) {

            List<Instant> timestamps = result.timestamps();
            List<Double> values = result.values();

            for (int i = 0; i < timestamps.size(); i++) {

                metricResults.put(timestamps.get(i).toString(), values.get(i).toString());
            }
        }

    }


    public Map<String, String> getMetricResults() {
        return this.metricResults;
    }


}
