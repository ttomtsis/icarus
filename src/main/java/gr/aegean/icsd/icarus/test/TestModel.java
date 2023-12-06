package gr.aegean.icsd.icarus.test;

import com.fasterxml.jackson.annotation.JsonProperty;
import gr.aegean.icsd.icarus.function.Function;
import gr.aegean.icsd.icarus.test.functionaltest.TestCase;
import gr.aegean.icsd.icarus.test.performancetest.LoadProfile;
import gr.aegean.icsd.icarus.test.performancetest.ResourceConfiguration;
import gr.aegean.icsd.icarus.user.IcarusUser;
import gr.aegean.icsd.icarus.util.enums.Metric;

import java.util.HashSet;
import java.util.Set;


public class TestModel {


    private String name;
    private Long testAuthor;
    private Long targetFunction;

    private String httpMethod;
    private String description;
    private String path;
    private String pathVariable;


    // Performance Test
    @JsonProperty("chosenMetrics")
    private final Set<Metric> chosenMetrics = new HashSet<>();
    private String pathVariableValue;
    private String requestBody;
    @JsonProperty("loadProfiles")
    private final Set<LoadProfile> loadProfiles = new HashSet<>();
    @JsonProperty("resourceConfigurations")
    private final Set<ResourceConfiguration> resourceConfigurations = new HashSet<>();


    // Functional Test
    private String functionURL;
    private Integer usedMemory;
    private String region;
    private final Set<TestCase> testCases = new HashSet<>();



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Set<Metric> getChosenMetrics() {
        return chosenMetrics;
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

    public Set<LoadProfile> getLoadProfiles() {
        return loadProfiles;
    }

    public Set<ResourceConfiguration> getResourceConfigurations() {
        return resourceConfigurations;
    }

    public String getFunctionURL() {
        return functionURL;
    }

    public void setFunctionURL(String functionURL) {
        this.functionURL = functionURL;
    }

    public Integer getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(Integer usedMemory) {
        this.usedMemory = usedMemory;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Set<TestCase> getTestCases() {
        return testCases;
    }


}
