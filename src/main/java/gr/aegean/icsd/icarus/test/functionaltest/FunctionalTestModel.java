package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.test.TestModel;

import java.util.Set;


public class FunctionalTestModel extends TestModel {


    private String functionUrl;
    private Set<Long> testCases;
    private Long resourceConfiguration;



    public FunctionalTestModel(TestModel parentModel , String functionUrl,
                               Set<Long> testCases,
                               Long resourceConfiguration) {

        super(parentModel);
        this.functionUrl = functionUrl;
        this.testCases = testCases;
        this.resourceConfiguration = resourceConfiguration;
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

    public Long getResourceConfiguration() {
        return resourceConfiguration;
    }

    public void setResourceConfiguration(Long resourceConfiguration) {
        this.resourceConfiguration = resourceConfiguration;
    }


}
