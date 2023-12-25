package gr.aegean.icsd.icarus.util.aws;

import gr.aegean.icsd.icarus.util.enums.Metric;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


public final class AwsMetricRequest {


    private final String accessKey;
    private final String secretKey;

    private final String functionName;
    private final String functionRegion;

    private final String metricName;
    private final String metricNamespace;
    private final String metricStatistic;

    private final HashMap<String, String> metricResults = new HashMap<>();

    private final Set<Instant> instants = new HashSet<>();



    public AwsMetricRequest(String awsAccessKey, String awsSecretKey, String lambdaFunctionName,
                            String functionRegion, Metric cloudwatchMetric) {

        this.accessKey = awsAccessKey;
        this.secretKey = awsSecretKey;

        this.functionName = lambdaFunctionName;
        this.functionRegion = functionRegion;

        this.metricName = cloudwatchMetric.getAwsMetricName();
        this.metricNamespace = cloudwatchMetric.getAwsNamespace();
        this.metricStatistic = cloudwatchMetric.getAwsStatistic();

        CloudWatchClient client = buildCloudwatchClient();
        GetMetricDataRequest request = createMetricRequest();

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
                                        .stat(this.metricStatistic)
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

                instants.add(timestamps.get(i));

                String formattedTimestamp = DateTimeFormatter.ofPattern("MMM dd yyyy h:mm:ss a")
                        .withZone(ZoneId.systemDefault())
                        .format(timestamps.get(i));

                metricResults.put(formattedTimestamp, String.valueOf(values.get(i).intValue()));
            }
        }

    }


    public Map<String, String> getMetricResults() {
        return this.metricResults;
    }

    public Set<Instant> getInstants() {
        return instants;
    }


}
