package gr.aegean.icsd.icarus.util.enums;


public enum Metric {


    INVOCATIONS("Invocations", "AWS/Lambda","cloudfunctions.googleapis.com/function/execution_count"),

    EXECUTION_TIME("Duration", "AWS/Lambda", "cloudfunctions.googleapis.com/function/execution_times"),

    CONCURRENT_EXECUTIONS("ConcurrentExecutions", "AWS/Lambda", "cloudfunctions.googleapis.com/function/instance_count"),

    REQUEST_COUNT("Count", "AWS/ApiGateway", "serviceruntime.googleapis.com/api/request_count"),

    REQUEST_LATENCIES("Latency", "AWS/ApiGateway", "serviceruntime.googleapis.com/api/request_latencies");


    private final String awsMetricName;
    private final String awsNamespace;
    private final String gcpMetricName;



    Metric(String awsMetricName, String awsNamespace, String gcpMetricName) {
        this.awsMetricName = awsMetricName;
        this.awsNamespace = awsNamespace;
        this.gcpMetricName = gcpMetricName;
    }



    public String getAwsMetricName() {
        return this.awsMetricName;
    }

    public String getAwsNamespace() {
        return this.awsNamespace;
    }

    public String getGcpMetricName() {
        return this.gcpMetricName;
    }


}
