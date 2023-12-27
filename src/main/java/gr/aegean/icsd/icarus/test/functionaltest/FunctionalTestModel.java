package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.test.TestModel;

import java.util.Set;


public class FunctionalTestModel extends TestModel {


    private String functionUrl;
    private Set<Long> testCases;
    private Set<Long> resourceConfigurations;



    public FunctionalTestModel(TestModel parentModel , String functionUrl,
                               Set<Long> testCases,
                               Set<Long> resourceConfigurations) {

        super(parentModel);
        this.functionUrl = functionUrl;
        this.testCases = testCases;
        this.resourceConfigurations = resourceConfigurations;
    }

    public FunctionalTestModel() {}



    public String getFunctionUrl() {
        return functionUrl;
    }

    public void setFunctionUrl(String functionUrl) {
        this.functionUrl = functionUrl;
    }

    public Set<Long> getTestCases() {
        return testCases;
    }

    public void setTestCases(Set<Long> testCases) {
        this.testCases = testCases;
    }

    public Set<Long> getResourceConfigurations() {
        return resourceConfigurations;
    }

    public void setResourceConfigurations(Set<Long> resourceConfigurations) {
        this.resourceConfigurations = resourceConfigurations;
    }


}
