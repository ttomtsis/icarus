package gr.aegean.icsd.icarus.util.gcp;


import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceSettings;
import com.google.monitoring.v3.*;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import gr.aegean.icsd.icarus.util.enums.Metric;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class GcpMetricRequest {


    private final String credentials;
    private final ProjectName projectName;
    private final String functionId;
    private final String metricId;

    private final HashMap<String, String> metricResults = new HashMap<>();



    public GcpMetricRequest(String credentials, String projectId, String functionId, Metric metric)
            throws IOException {

        this.credentials = credentials;
        this.projectName = ProjectName.of(projectId);

        this.functionId = functionId;
        this.metricId = metric.getGcpMetricName();


        try (MetricServiceClient client = MetricServiceClient.create(createMetricServiceSettings())) {

            ListTimeSeriesRequest listTimeSeriesRequest = createRequest();

            MetricServiceClient.ListTimeSeriesPagedResponse response = client.listTimeSeries(listTimeSeriesRequest);

            getMetricResults(response);
        }

    }



    private MetricServiceSettings createMetricServiceSettings() throws IOException {

        InputStream inputStream = new ByteArrayInputStream(this.credentials.getBytes(StandardCharsets.UTF_8));

        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(inputStream);

        return MetricServiceSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials))
                .build();

    }

    private TimeInterval createTimeInterval() {

        Instant now = Instant.now();
        Instant oneHourAgo = now.minusSeconds(60 * 60);

        return TimeInterval.newBuilder()
                .setEndTime(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano()))
                .setStartTime(Timestamp.newBuilder()
                        .setSeconds(oneHourAgo.getEpochSecond())
                        .setNanos(oneHourAgo.getNano()))
                .build();
    }

    private ListTimeSeriesRequest createRequest() {

        String filter = "metric.type=\"" + this.metricId + "\" AND resource.labels.function_name=\"" + functionId + "\"";

        if (this.metricId.equals(Metric.EXECUTION_TIME.getGcpMetricName())) {

            return ListTimeSeriesRequest.newBuilder()
                    .setName(projectName.toString())
                    .setFilter(filter)
                    .setInterval(createTimeInterval())
                    .setView(ListTimeSeriesRequest.TimeSeriesView.FULL)
                    .setAggregation(
                            Aggregation.newBuilder()
                                .setAlignmentPeriod(Duration.newBuilder().setSeconds(60).build())
                                .setCrossSeriesReducer(Aggregation.Reducer.REDUCE_MEAN)
                                .setPerSeriesAligner(Aggregation.Aligner.ALIGN_PERCENTILE_99)
                            .build()
                    )
                    .build();
        }

        return ListTimeSeriesRequest.newBuilder()
                .setName(projectName.toString())
                .setFilter(filter)
                .setInterval(createTimeInterval())
                .setView(ListTimeSeriesRequest.TimeSeriesView.FULL)
                .build();
    }

    private void getMetricResults(MetricServiceClient.ListTimeSeriesPagedResponse response) {

        for (TimeSeries responseTimeSeries : response.iterateAll()) {

            for (Point dataPoint : responseTimeSeries.getPointsList()) {

                Timestamp timestamp = dataPoint.getInterval().getStartTime();
                Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());

                String formattedTimestamp = DateTimeFormatter.ofPattern("MMM dd yyyy h:mm:ss a")
                        .withZone(ZoneId.systemDefault())
                        .format(instant);

                String formattedValue = String.valueOf(dataPoint.getValue().getInt64Value());

                if (this.metricId.equals(Metric.EXECUTION_TIME.getGcpMetricName())) {
                    formattedValue = dataPoint.getValue().getDoubleValue() / 1_000_000 + "ms";
                }

                metricResults.put(formattedTimestamp, formattedValue);
            }

        }
    }


    public Map<String, String> getMetricResults() {
        return this.metricResults;
    }


}
