package gr.aegean.icsd.icarus.test.performancetest;

import gr.aegean.icsd.icarus.provideraccount.AwsAccount;
import gr.aegean.icsd.icarus.provideraccount.GcpAccount;
import gr.aegean.icsd.icarus.test.performancetest.loadprofile.LoadProfile;
import gr.aegean.icsd.icarus.testexecution.metricresult.MetricResult;
import gr.aegean.icsd.icarus.util.aws.AwsMetricRequest;
import gr.aegean.icsd.icarus.util.enums.Metric;
import gr.aegean.icsd.icarus.util.enums.Platform;
import gr.aegean.icsd.icarus.util.exceptions.test.MetricsTimeoutException;
import gr.aegean.icsd.icarus.util.exceptions.test.TestExecutionFailedException;
import gr.aegean.icsd.icarus.util.gcp.GcpMetricRequest;
import gr.aegean.icsd.icarus.util.jmeter.LoadTest;
import gr.aegean.icsd.icarus.util.terraform.DeploymentRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.METRIC_QUERY_MAX_TIMEOUT;


public class MetricQueryEngine {


    private final Set<LoadProfile> loadProfiles;
    private final Set<Metric> chosenMetrics;

    private final Set<MetricResult> resultList = Collections.synchronizedSet(new HashSet<>());


    private static final Logger log = LoggerFactory.getLogger("Metric Query Engine");



    public MetricQueryEngine(PerformanceTest requestedTest, Set<DeploymentRecord> deploymentRecords) {

        this.loadProfiles = requestedTest.getLoadProfiles();
        this.chosenMetrics = requestedTest.getChosenMetrics();

        List<Thread> threadList = new ArrayList<>();

        for (DeploymentRecord deploymentRecord : deploymentRecords) {

            LoadTest test = createLoadTest(requestedTest, deploymentRecord);

            threadList.add(runLoadTest(test, deploymentRecord));
        }

        log.warn("Waiting for all tests to execute");
        waitForThreadsToFinish(threadList);

        log.warn("All Load Tests have been executed");
    }



    private LoadTest createLoadTest(PerformanceTest requestedTest, DeploymentRecord deploymentRecord) {

        LoadTest test = new LoadTest(deploymentRecord.deployedFunctionName, deploymentRecord.deployedUrl,
                requestedTest.getPath(), requestedTest.getPathVariable(),
                requestedTest.getPathVariableValue(), HttpMethod.valueOf(requestedTest.getHttpMethod()));

        for (LoadProfile loadProfile : requestedTest.getLoadProfiles()) {

            test.addLoadProfile(loadProfile.getConcurrentUsers(), loadProfile.getRampUp(),
                    loadProfile.getLoadTime(), loadProfile.getThinkTime(), loadProfile.getStartDelay());
        }

        return test;
    }



    private Thread runLoadTest(LoadTest test, DeploymentRecord deploymentRecord) {

        Thread loadTestExecutionThread = new Thread(() -> {

            log.warn("Executing Load Test for: " + deploymentRecord.deployedFunctionName);
            test.runTest();

            Instant testDoneInstant = Instant.now();

            log.warn("Test finished at " + formatInstant(testDoneInstant));
            log.warn("Test Completed, thread will sleep until metrics are logged in provider platform");

            sleep(2);

            log.warn("Thread has awoken, querying metrics for " + deploymentRecord.deployedFunctionName);

            List<Thread> metricQueryThreadsList = createMetrics(deploymentRecord, testDoneInstant);
            waitForThreadsToFinish(metricQueryThreadsList);

        });

        loadTestExecutionThread.start();
        return loadTestExecutionThread;
    }


    private List<Thread> createMetrics(DeploymentRecord deploymentRecord, Instant testCompletionInstant) {

        List<Thread> threadList = new ArrayList<>();

        for (Metric metric : chosenMetrics) {

            Platform platform = deploymentRecord.deployedPlatform;

            Thread thread = new Thread(() -> {

                log.warn("Creating metric: " + metric.toString() + " for test: " + deploymentRecord.deployedFunctionName);

                if (platform.equals(Platform.AWS)) {

                    AwsAccount awsAccount = (AwsAccount) deploymentRecord.accountUsed;

                    createAwsMetricRequest(deploymentRecord, awsAccount, metric, testCompletionInstant);
                }

                if (platform.equals(Platform.GCP)) {

                    GcpAccount gcpAccount = (GcpAccount) deploymentRecord.accountUsed;

                    createGcpMetricRequest(deploymentRecord, gcpAccount, metric, testCompletionInstant);
                }
            });

            threadList.add(thread);
            thread.start();
        }

        return threadList;
    }


    private void createAwsMetricRequest(DeploymentRecord deploymentRecord, AwsAccount account,
                                        Metric metric, Instant testDone) {


        AwsMetricRequest metricRequest = new AwsMetricRequest(account.getAwsAccessKey(), account.getAwsSecretKey(),
                deploymentRecord.deployedFunctionName, deploymentRecord.deployedRegion,
                metric);

        int minutes = 0;
        boolean foundMetrics = compareTimestamps(metricRequest.getInstants(), testDone);
        while (!foundMetrics && minutes < METRIC_QUERY_MAX_TIMEOUT) {

            log.warn("Metric " + metric + " has not been logged yet for deployment id: " + deploymentRecord.deploymentGuid
                    + ", will sleep and retry");

            minutes++;

            log.warn(minutes + " minutes have passed");

            sleep(1);

            metricRequest = new AwsMetricRequest(account.getAwsAccessKey(), account.getAwsSecretKey(),
                    deploymentRecord.deployedFunctionName, deploymentRecord.deployedRegion,
                    metric);

            foundMetrics = compareTimestamps(metricRequest.getInstants(), testDone);
        }

        if (foundMetrics) {
            resultList.add(new MetricResult(loadProfiles, deploymentRecord.configurationUsed,
                    metricRequest.getMetricResults(), metric.toString()));
        }
        else {
            throw new MetricsTimeoutException(minutes);
        }

    }


    private void createGcpMetricRequest(DeploymentRecord deploymentRecord, GcpAccount account, Metric metric,
                                        Instant testDone) {

        try {
            GcpMetricRequest request = new GcpMetricRequest(account.getGcpKeyfile(), account.getGcpProjectId(),
                    deploymentRecord.deployedFunctionName, metric);

            int minutes = 0;
            boolean foundMetrics = compareTimestamps(request.getInstants(), testDone);
            while (!foundMetrics && minutes < METRIC_QUERY_MAX_TIMEOUT) {

                log.warn("Metrics are not logged, will sleep and retry");

                minutes++;

                log.warn(minutes + " minutes have passed");

                sleep(1);

                request = new GcpMetricRequest(account.getGcpKeyfile(), account.getGcpProjectId(),
                        deploymentRecord.deployedFunctionName, metric);

                foundMetrics = compareTimestamps(request.getInstants(), testDone);
            }

            if (foundMetrics) {
                resultList.add(new MetricResult(loadProfiles, deploymentRecord.configurationUsed,
                        request.getMetricResults(), metric.toString()));
            }
            else {
                throw new MetricsTimeoutException(minutes);
            }

        }
        catch (IOException ex) {
            throw new TestExecutionFailedException(ex);
        }

    }



    private String formatInstant(Instant instant) {

        return DateTimeFormatter.ofPattern("MMM dd yyyy h:mm:ss a")
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }

    private void sleep(int minutes) {

        try {
            Thread.sleep(minutes * 60000L);
        } catch (InterruptedException e) {
            throw new TestExecutionFailedException(e);
        }
    }

    private void waitForThreadsToFinish(List<Thread> threadList) {

        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new TestExecutionFailedException(e);
            }
        }
    }

    private boolean compareTimestamps(Set<Instant> instants, Instant testExecutionCompleted) {

        for (Instant currentInstant : instants) {

            if (testExecutionCompleted.isBefore(currentInstant) ||
                    testExecutionCompleted.truncatedTo(ChronoUnit.MINUTES)
                            .equals(currentInstant.truncatedTo(ChronoUnit.MINUTES))
            ) {

                return true;
            }

        }

        return false;
    }


    public Set<MetricResult> getResultList() {
        return resultList;
    }


}
