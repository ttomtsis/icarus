package gr.aegean.icsd.icarus.util.services;

import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.testexecution.TestExecution;
import gr.aegean.icsd.icarus.testexecution.metricresult.MetricResult;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


@Service
public class RegressionService {


    private static final Logger log = LoggerFactory.getLogger(RegressionService.class);



    public String applyLinearRegression(TestExecution testExecution) {

        StringBuilder regressionEquation = new StringBuilder("Linear regressions for metrics:\n");

        // Find the metrics that are used by the execution
        Set<String> usedMetrics = findUsedMetrics(testExecution);

        // Find the regions and the runtimes
        Set<String> usedRegions = findUsedRegions(testExecution);
        Set<String> usedRuntimes = findRuntimesUsed(testExecution);

        // Label the runtimes and the regions
        Map<String, Integer> regionLabels = labelEncodeSet(usedRegions);
        Map<String, Integer> runtimeLabels = labelEncodeSet(usedRuntimes);

        for (String metric : usedMetrics) {

            OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
            ArrayList<Double> yList = new ArrayList<>();
            ArrayList<double[]> xList = new ArrayList<>();


            for (MetricResult result : testExecution.getMetricResults()) {

                if (result.getMetricName().equals(metric)) {

                    // Calculate and add y and x to the lists
                    yList.add(calculateAverageOfMetricResultValues(result));
                    xList.addAll(extractPredictorsFromMetricResult(result, regionLabels, runtimeLabels));
                }
            }

            double[] y = new double[yList.size()];
            for (int i = 0; i < yList.size(); i++) {
                y[i] = yList.get(i);
            }

            double[][] x = new double[xList.size()][];
            for (int i = 0; i < xList.size(); i++) {
                x[i] = xList.get(i);
            }

            regressionEquation.append("Metric: " + metric + "\n");

            try{
                regression.newSampleData(y, x);

                double[] coefficients = regression.estimateRegressionParameters();

                regressionEquation.append("y = (" + coefficients[1] + "*memory) + (" + coefficients[2] + "*region) + (" + coefficients[3]
                        + "*runtime) + " + coefficients[0]);
                regressionEquation.append("\n");
            }
            catch (RuntimeException ex) {
                log.error("Regression Failed: {}", ex.getMessage());
                regressionEquation.append("Unable to perform regression: " + ex.getMessage());
            }

        }

        return regressionEquation.toString();
    }



    private Set<String> findUsedMetrics(TestExecution testExecution) {

        Set<String> metricsUsed = new HashSet<>();

        for (MetricResult metricResult : testExecution.getMetricResults()) {

            metricsUsed.add(metricResult.getMetricName());
        }

        return metricsUsed;
    }

    private Set<String> findUsedRegions(TestExecution testExecution) {

        Set<String> regionsUsed = new HashSet<>();

        for (MetricResult metricResult : testExecution.getMetricResults()) {

            regionsUsed.addAll(metricResult.getResourceConfiguration().getRegions());
        }

        return regionsUsed;
    }

    private Set<String> findRuntimesUsed(TestExecution testExecution) {

        Set<String> runtimesUsed = new HashSet<>();

        for (MetricResult metricResult : testExecution.getMetricResults()) {

            runtimesUsed.add(metricResult.getResourceConfiguration().getFunctionRuntime());
        }

        return runtimesUsed;
    }

    private Map<String, Integer> labelEncodeSet(Set<String> rawSet) {

        HashMap<String, Integer> encodedSet = new HashMap<>();

        for (String rawValue : rawSet) {

            int randomInt = ThreadLocalRandom.current().nextInt(1, 20000 + 1);

            encodedSet.put(rawValue, randomInt);
        }

        return encodedSet;
    }

    private double calculateAverageOfMetricResultValues(MetricResult metricResult) {

        double average = 0.0;

        for (String value : metricResult.getMetricValues().values()) {

            double doubleValue = Double.parseDouble(value);
            average = average + doubleValue;
        }

        return average/metricResult.getMetricValues().values().size();
    }

    private  ArrayList<double[]> extractPredictorsFromMetricResult(MetricResult metricResult,
                                                         Map<String, Integer> regionLabels,
                                                         Map<String, Integer> runtimeLabels) {

        ArrayList<double[]> predictorList = new ArrayList<>();

        ResourceConfiguration associatedConfiguration = metricResult.getResourceConfiguration();

        for (Integer memory : associatedConfiguration.getMemoryConfigurations()) {
            for (String region : associatedConfiguration.getRegions()) {

                // Create entry
                double[] newEntry = {
                        memory,
                        regionLabels.get(region),
                        runtimeLabels.get(associatedConfiguration.getFunctionRuntime())
                };

                // Add entry to predictors
                predictorList.add(newEntry);
            }
        }

        // Convert ArrayList<double[]> to double[][]
        double[][] predictors = new double[predictorList.size()][];
        for (int i = 0; i < predictorList.size(); i++) {
            predictors[i] = predictorList.get(i);
        }

        return predictorList;
    }


}
