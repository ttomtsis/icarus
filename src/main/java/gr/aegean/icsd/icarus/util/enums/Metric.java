package gr.aegean.icsd.icarus.util.enums;


public enum Metric {


    INVOCATIONS("Invocations", "AWS/Lambda", "Sum",
            "cloudfunctions.googleapis.com/function/execution_count"),

    EXECUTION_TIME("Duration", "AWS/Lambda", "Maximum",
            "cloudfunctions.googleapis.com/function/execution_times"),

    CONCURRENT_EXECUTIONS("ConcurrentExecutions", "AWS/Lambda", "Maximum",
            "cloudfunctions.googleapis.com/function/active_instances");



    private final String awsMetricName;
    private final String awsNamespace;
    private final String awsStatistic;
    private final String gcpMetricName;



    Metric(String awsMetricName, String awsNamespace, String awsStatistic, String gcpMetricName) {
        this.awsMetricName = awsMetricName;
        this.awsNamespace = awsNamespace;
        this.awsStatistic = awsStatistic;
        this.gcpMetricName = gcpMetricName;
    }



    public String getAwsMetricName() {
        return this.awsMetricName;
    }

    public String getAwsNamespace() {
        return this.awsNamespace;
    }

    public String getAwsStatistic() {
        return this.awsStatistic;
    }

    public String getGcpMetricName() {
        return this.gcpMetricName;
    }


}
