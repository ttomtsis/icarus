package gr.aegean.icsd.icarus.util.gcp;


import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceSettings;
import com.google.monitoring.v3.ListTimeSeriesRequest;
import com.google.monitoring.v3.Point;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.protobuf.Timestamp;

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
    private final String projectName;
    private final String functionId;
    private final String metricId;

    private final HashMap<String, String> metricResults = new HashMap<>();



    public GcpMetricRequest(String credentials, String projectId, String functionId, String metricId)
            throws IOException {

        this.credentials = credentials;
        this.projectName = projectId;
        this.functionId = functionId;
        this.metricId = metricId;

        try (MetricServiceClient metricServiceClient = MetricServiceClient.create(createMetricServiceSettings())) {

            ListTimeSeriesRequest request = createRequest();

            MetricServiceClient.ListTimeSeriesPagedResponse response = metricServiceClient.listTimeSeries(request);

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

        String filter = "metric.type=\"" + metricId + "\" AND resource.labels.function_name=\"" + functionId + "\"";

        return ListTimeSeriesRequest.newBuilder()
                .setName(projectName)
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

                metricResults.put(formattedTimestamp, String.valueOf(dataPoint.getValue()));
            }

        }
    }


    public Map<String, String> getMetricResults() {
        return this.metricResults;
    }


}
