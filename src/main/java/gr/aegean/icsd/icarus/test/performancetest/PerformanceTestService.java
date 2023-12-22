package gr.aegean.icsd.icarus.test.performancetest;

import gr.aegean.icsd.icarus.provideraccount.AwsAccount;
import gr.aegean.icsd.icarus.provideraccount.GcpAccount;
import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.test.TestService;
import gr.aegean.icsd.icarus.test.performancetest.loadprofile.LoadProfile;
import gr.aegean.icsd.icarus.testexecution.MetricResult;
import gr.aegean.icsd.icarus.testexecution.MetricResultRepository;
import gr.aegean.icsd.icarus.util.aws.AwsMetricRequest;
import gr.aegean.icsd.icarus.util.aws.AwsRegion;
import gr.aegean.icsd.icarus.util.enums.Metric;
import gr.aegean.icsd.icarus.util.enums.Platform;
import gr.aegean.icsd.icarus.util.enums.TestState;
import gr.aegean.icsd.icarus.util.exceptions.test.InvalidTestConfigurationException;
import gr.aegean.icsd.icarus.util.exceptions.test.TestExecutionFailedException;
import gr.aegean.icsd.icarus.util.gcp.GcpMetricRequest;
import gr.aegean.icsd.icarus.util.jmeter.LoadTest;
import gr.aegean.icsd.icarus.util.terraform.CompositeKey;
import gr.aegean.icsd.icarus.util.terraform.StackDeployer;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.*;


@Service
@Transactional
@Validated
public class PerformanceTestService extends TestService {


    private final TestRepository repository;
    private final MetricResultRepository metricResultRepository;
    
    private static final Logger log = LoggerFactory.getLogger("Performance Test Service");



    public PerformanceTestService(TestRepository repository, StackDeployer deployer,
                                  MetricResultRepository metricResultRepository) {

        super(repository, deployer);
        this.repository = repository;
        this.metricResultRepository = metricResultRepository;
    }



    @Override
    public PerformanceTest searchTest(@NotNull @Positive Long testId) {

        return (PerformanceTest) super.searchTest(testId);
    }

    public PerformanceTest createTest(@NotNull PerformanceTest newTest) {

        if (!StringUtils.isBlank(newTest.getPathVariableValue()) &&
                StringUtils.isBlank(newTest.getPath())) {

            throw new InvalidTestConfigurationException
                    ("Cannot set a Path variable value if the test does not expose a path");
        }

        if (newTest.getChosenMetrics() == null) {
            throw new InvalidTestConfigurationException
                    ("A Performance test must utilize at least 1 Metric");
        }

        return (PerformanceTest) super.createTest(newTest);
    }

    public void updateTest(@NotNull @Positive Long testId, @NotNull PerformanceTestModel testModel) {

        PerformanceTest requestedTest = (PerformanceTest) super.updateTest(testId, testModel);

        super.setIfNotBlank(requestedTest::setPathVariableValue, testModel.getPathVariableValue());
        super.setIfNotBlank(requestedTest::setRequestBody, testModel.getRequestBody());

        if (testModel.getChosenMetrics() != null) {
            requestedTest.setChosenMetrics(testModel.getChosenMetrics());
        }

        repository.save(requestedTest);
    }

    @Override
    public Test executeTest(@NotNull @Positive Long testId) {

        PerformanceTest requestedTest = (PerformanceTest) super.executeTest(testId);

        if (requestedTest.getLoadProfiles().isEmpty()) {
            throw new InvalidTestConfigurationException(testId, " does not have any Load Profiles" +
                    " associated with it");
        }

        if (requestedTest.getChosenMetrics().isEmpty()) {
            throw new InvalidTestConfigurationException(testId, " does not have any Metrics" +
                    " associated with it");
        }

        super.setState(requestedTest, TestState.DEPLOYING);

        String deploymentId = UUID.randomUUID().toString().substring(0, 5);

        super.getDeployer().deploy(requestedTest, deploymentId)

            .exceptionally(ex -> {
                super.setState(requestedTest, TestState.ERROR);
                throw new TestExecutionFailedException(ex);
            })

            .thenAccept(result -> {

                super.setState(requestedTest, TestState.RUNNING);

                try {
                    
                    log.warn("Creating Load Tests");
                    createAndRunLoadTests(requestedTest, result, deploymentId);

                } catch (RuntimeException ex) {

                    super.setState(requestedTest, TestState.ERROR);
                    throw new TestExecutionFailedException(ex);
                }
                
                log.warn("Test Completed, Deleting Stack");

                super.getDeployer().deleteStack(requestedTest.getTargetFunction().getName(), deploymentId);
                super.setState(requestedTest, TestState.FINISHED);

                log.warn("Finished");
            });

        return null;
    }


    private void createAndRunLoadTests(PerformanceTest requestedTest,
                                       HashMap<CompositeKey, String> functionUrls,
                                       String deploymentId) {

        Set<Thread> threadSet = new HashSet<>();

        for (Map.Entry<CompositeKey, String> entry : functionUrls.entrySet()) {

            LoadTest test = createLoadTest(requestedTest, entry);

            Thread newThread = runLoadTest(test, requestedTest, entry.getKey(), deploymentId);
            threadSet.add(newThread);
        }

        for (Thread thread : threadSet) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new TestExecutionFailedException(e);
            }
        }

        log.warn("All Load Tests have been executed");
    }

    private LoadTest createLoadTest(PerformanceTest requestedTest, Map.Entry<CompositeKey, String> entry) {

        LoadTest test = new LoadTest(requestedTest.getName(), entry.getValue(),
                requestedTest.getPath(), requestedTest.getPathVariable(),
                requestedTest.getPathVariableValue(), HttpMethod.valueOf(requestedTest.getHttpMethod()));

        for (LoadProfile loadProfile : requestedTest.getLoadProfiles()) {

            test.addLoadProfile(loadProfile.getConcurrentUsers(), loadProfile.getRampUp(),
                    loadProfile.getLoadTime(), loadProfile.getThinkTime(), loadProfile.getStartDelay());

        }

        return test;
    }

    private Thread runLoadTest(LoadTest test, PerformanceTest requestedTest, CompositeKey key, String deploymentId) {

        Thread thread = new Thread(() -> {
            
            log.warn("Executing Load Test");
            test.runTest();
            
            log.warn("Test Completed, thread will sleep until metrics are logged in provider platform");
            try {
                Thread.sleep(120000);
            } catch (InterruptedException e) {
                throw new TestExecutionFailedException(e);
            }
            
            log.warn("Thread has awoken, querying metrics");
            createMetrics(requestedTest, key, requestedTest.getLoadProfiles(), deploymentId);
        });

        thread.start();

        return thread;
    }


    private void createMetrics(PerformanceTest requestedTest, CompositeKey key,
                               Set<LoadProfile> profiles, String deploymentId) {

        for (Metric metric : requestedTest.getChosenMetrics()) {

            if (key.configurationUsed().getProviderPlatform().equals(Platform.AWS)) {

                AwsAccount awsAccount = (AwsAccount) key.accountUsed();
                Set<AwsMetricRequest> requests = createAwsMetricRequests(key, awsAccount, metric);

                for (AwsMetricRequest request : requests) {
                    request.sendRequest();
                    metricResultRepository.save(new MetricResult(profiles, key.configurationUsed(),
                            request.getMetricResults(), metric.toString(), deploymentId));
                }
            }

            if (key.configurationUsed().getProviderPlatform().equals(Platform.GCP)) {

                GcpAccount gcpAccount = (GcpAccount) key.accountUsed();
                GcpMetricRequest request = createGcpMetricRequest(gcpAccount, requestedTest, metric);

                request.sendRequest();
                metricResultRepository.save(new MetricResult(profiles, key.configurationUsed(),
                        request.getMetricResults(), metric.toString(), deploymentId));
            }
        }

    }

    private Set<AwsMetricRequest> createAwsMetricRequests(CompositeKey key, AwsAccount account,
                                                          Metric metric) {

        Set<AwsMetricRequest> requests = new HashSet<>();
        for (String region : key.configurationUsed().getRegions()) {

            AwsMetricRequest metricRequest = new AwsMetricRequest(account.getAwsAccessKey(), account.getAwsSecretKey(),
                    key.outputName(), AwsRegion.valueOf(region),
                    metric);
            requests.add(metricRequest);
        }

        return requests;
    }

    private GcpMetricRequest createGcpMetricRequest(GcpAccount account, PerformanceTest test, Metric metric) {

        try {
            return  new GcpMetricRequest(account.getGcpKeyfile(), account.getGcpProjectId(),
                    test.getTargetFunction().getName(), metric);
        }
        catch (IOException ex) {
            throw new TestExecutionFailedException(ex);
        }

    }


}
