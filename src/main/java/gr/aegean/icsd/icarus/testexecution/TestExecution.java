package gr.aegean.icsd.icarus.testexecution;

import gr.aegean.icsd.icarus.report.Report;
import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.testexecution.metricresult.MetricResult;
import gr.aegean.icsd.icarus.testexecution.testcaseresult.TestCaseResult;
import gr.aegean.icsd.icarus.user.IcarusUser;
import gr.aegean.icsd.icarus.util.enums.TestState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "test_execution")
@EntityListeners(AuditingEntityListener.class)
public class TestExecution {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedBy
    @ManyToOne
    @JoinColumn(updatable = false)
    private IcarusUser creator;

    @OneToOne(targetEntity = Report.class, cascade = {CascadeType.REFRESH, CascadeType.PERSIST, CascadeType.REMOVE})
    private Report report;

    @NotNull(message = "Test result's start date cannot be null")
    private Instant startDate;

    private Instant endDate;

    @NotNull(message = "Test result's deployment id cannot be null")
    private String deploymentId;

    @ManyToOne(targetEntity = Test.class, optional = false)
    private Test parentTest;

    @OneToMany(targetEntity = TestCaseResult.class, orphanRemoval = true,
            cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn
    private final Set<TestCaseResult> testCaseResults = new HashSet<>();

    @OneToMany(targetEntity = MetricResult.class, orphanRemoval = true,
            cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn
    private final Set<MetricResult> metricResults = new HashSet<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    private TestState state;



    public TestExecution(Test parentTest, Instant startDate, String deploymentId) {

        this.parentTest = parentTest;
        this.startDate = startDate;
        this.deploymentId = deploymentId;

        this.state = TestState.CREATED;
    }

    public TestExecution() {this.state = TestState.CREATED;}



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

    public TestState getState() {
        return state;
    }

    public void setState(TestState state) {
        this.state = state;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public IcarusUser getCreator() {
        return creator;
    }

    public void setCreator(IcarusUser creator) {
        this.creator = creator;
    }


}
