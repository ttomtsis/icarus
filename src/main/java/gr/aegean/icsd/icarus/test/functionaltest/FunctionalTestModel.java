package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCaseModel;
import gr.aegean.icsd.icarus.util.enums.Platform;
import org.springframework.hateoas.RepresentationModel;

import java.util.HashSet;
import java.util.Set;


public class FunctionalTestModel extends RepresentationModel<FunctionalTestModel> {


    private Long id;
    private String name;
    private String description;
    private String httpMethod;
    private String path;
    private String pathVariable;
    private Long testAuthor;
    private Long targetFunction;

    private String region;
    private Integer usedMemory;
    private String functionUrl;
    private Set<TestCaseModel> testCases = new HashSet<>();
    private Platform providerPlatform;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Platform getProviderPlatform() {
        return providerPlatform;
    }

    public void setProviderPlatform(Platform providerPlatform) {
        this.providerPlatform = providerPlatform;
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(Integer usedMemory) {
        this.usedMemory = usedMemory;
    }

    public String getFunctionUrl() {
        return functionUrl;
    }

    public void setFunctionUrl(String functionUrl) {
        this.functionUrl = functionUrl;
    }

    public Set<TestCaseModel> getTestCases() {
        return testCases;
    }

    public void setTestCases(Set<TestCaseModel> testCases) {
        this.testCases = testCases;
    }


}
