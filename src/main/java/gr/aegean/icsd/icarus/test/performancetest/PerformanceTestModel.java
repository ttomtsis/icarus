package gr.aegean.icsd.icarus.test.performancetest;

import gr.aegean.icsd.icarus.test.TestModel;
import gr.aegean.icsd.icarus.util.enums.Metric;

import java.util.Set;

public class PerformanceTestModel extends TestModel {


    private Set<Metric> chosenMetrics;
    private String pathVariableValue;
    private String requestBody;
    private Set<Long> loadProfiles;
    private Set<Long> resourceConfigurations;



    public PerformanceTestModel (TestModel parentModel, Set<Metric> chosenMetrics, String pathVariableValue,
                                 String requestBody, Set<Long> loadProfiles,
                                 Set<Long> resourceConfigurations) {

        super(parentModel);
        this.chosenMetrics = chosenMetrics;
        this.pathVariableValue = pathVariableValue;
        this.requestBody = requestBody;
        this.loadProfiles = loadProfiles;
        this.resourceConfigurations = resourceConfigurations;
    }

    public PerformanceTestModel() {}



    public Set<Metric> getChosenMetrics() {
        return chosenMetrics;
    }

    public void setChosenMetrics(Set<Metric> chosenMetrics) {
        this.chosenMetrics = chosenMetrics;
    }

    public String getPathVariableValue() {
        return pathVariableValue;
    }

    public void setPathVariableValue(String pathVariableValue) {
        this.pathVariableValue = pathVariableValue;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public Set<Long> getLoadProfiles() {
        return loadProfiles;
    }

    public void setLoadProfiles(Set<Long> loadProfiles) {
        this.loadProfiles = loadProfiles;
    }

    public Set<Long> getResourceConfigurations() {
        return resourceConfigurations;
    }

    public void setResourceConfigurations(Set<Long> resourceConfigurations) {
        this.resourceConfigurations = resourceConfigurations;
    }


}
