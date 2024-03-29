package gr.aegean.icsd.icarus.testexecution;

import gr.aegean.icsd.icarus.util.enums.ExecutionState;
import org.springframework.hateoas.RepresentationModel;

import java.util.Set;

public class TestExecutionModel extends RepresentationModel<TestExecutionModel> {


    private Long id;
    private String creator;
    private Long report;
    private String startDate;
    private String endDate;
    private Long parentTest;
    private Set<Long> testCaseResults;
    private Set<Long> metricResults;
    private ExecutionState testState;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReport() {
        return report;
    }

    public void setReport(Long report) {
        this.report = report;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Long getParentTest() {
        return parentTest;
    }

    public void setParentTest(Long parentTest) {
        this.parentTest = parentTest;
    }

    public Set<Long> getTestCaseResults() {
        return testCaseResults;
    }

    public void setTestCaseResults(Set<Long> testCaseResults) {
        this.testCaseResults = testCaseResults;
    }

    public Set<Long> getMetricResults() {
        return metricResults;
    }

    public void setMetricResults(Set<Long> metricResults) {
        this.metricResults = metricResults;
    }

    public ExecutionState getTestState() {
        return testState;
    }

    public void setTestState(ExecutionState testState) {
        this.testState = testState;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }


}
