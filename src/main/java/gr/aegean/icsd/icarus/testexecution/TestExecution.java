package gr.aegean.icsd.icarus.testexecution;

import gr.aegean.icsd.icarus.report.Report;
import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.testexecution.metricresult.MetricResult;
import gr.aegean.icsd.icarus.testexecution.testcaseresult.TestCaseResult;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "test_execution")
public class TestExecution {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(targetEntity = Report.class, cascade = {CascadeType.REFRESH, CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "test_report")
    private Report report;

    @NotNull(message = "Test result's start date cannot be null")
    private Instant startDate;

    @NotNull(message = "Test result's end date cannot be null")
    private Instant endDate;


    @ManyToOne(targetEntity = Test.class, optional = false)
    private Test parentTest;

    @OneToMany(targetEntity = TestCaseResult.class, orphanRemoval = true,
            cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "test_case_results")
    private final Set<TestCaseResult> testCaseResults = new HashSet<>();

    @OneToMany(targetEntity = MetricResult.class, orphanRemoval = true,
            cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "metric_results")
    private final Set<MetricResult> metricResults = new HashSet<>();



    public TestExecution(Test parentTest, Report report, Instant startDate, Instant endDate) {

        this.parentTest = parentTest;
        this.report = report;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public TestExecution() {}



    public void addTestCaseResults(Set<TestCaseResult> testCaseResults) {

        this.testCaseResults.addAll(testCaseResults);
    }

    public void addMetricResults(Set<MetricResult> metricResults) {

        this.metricResults.addAll(metricResults);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public Test getParentTest() {
        return parentTest;
    }

    public void setParentTest(Test parentTest) {
        this.parentTest = parentTest;
    }

    public Set<TestCaseResult> getTestCaseResults() {
        return testCaseResults;
    }

    public Set<MetricResult> getMetricResults() {
        return metricResults;
    }
}
