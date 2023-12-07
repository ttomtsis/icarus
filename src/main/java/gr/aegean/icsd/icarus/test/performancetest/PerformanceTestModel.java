package gr.aegean.icsd.icarus.test.performancetest;

import gr.aegean.icsd.icarus.test.performancetest.loadprofile.LoadProfileModel;
import gr.aegean.icsd.icarus.test.performancetest.resourceconfiguration.ResourceConfigurationModel;
import gr.aegean.icsd.icarus.util.enums.Metric;
import org.springframework.hateoas.RepresentationModel;

import java.util.HashSet;
import java.util.Set;

public class PerformanceTestModel extends RepresentationModel<PerformanceTestModel> {


    private Long id;
    private String name;
    private String description;
    private String httpMethod;
    private String path;
    private String pathVariable;
    private Long testAuthor;
    private Long targetFunction;

    private Set<Metric> chosenMetrics = new HashSet<>();
    private String pathVariableValue;
    private String requestBody;
    private Set<LoadProfileModel> loadProfiles = new HashSet<>();
    private Set<ResourceConfigurationModel> resourceConfigurations = new HashSet<>();



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPathVariable() {
        return pathVariable;
    }

    public void setPathVariable(String pathVariable) {
        this.pathVariable = pathVariable;
    }

    public Long getTestAuthor() {
        return testAuthor;
    }

    public void setTestAuthor(Long testAuthor) {
        this.testAuthor = testAuthor;
    }

    public Long getTargetFunction() {
        return targetFunction;
    }

    public void setTargetFunction(Long targetFunction) {
        this.targetFunction = targetFunction;
    }

    public Set<Metric> getChosenMetrics() {
        return chosenMetrics;
    }

    public void setChosenMetrics(Set<Metric> chosenMetrics) {
        this.chosenMetrics = chosenMetrics;
    }

    public void setLoadProfiles(Set<LoadProfileModel> loadProfiles) {
        this.loadProfiles = loadProfiles;
    }

    public void setResourceConfigurations(Set<ResourceConfigurationModel> resourceConfigurations) {
        this.resourceConfigurations = resourceConfigurations;
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

    public Set<LoadProfileModel> getLoadProfiles() {
        return loadProfiles;
    }

    public Set<ResourceConfigurationModel> getResourceConfigurations() {
        return resourceConfigurations;
    }


}
