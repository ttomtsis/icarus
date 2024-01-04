package gr.aegean.icsd.icarus.testexecution.testcaseresult;

import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.test.functionaltest.testcasemember.TestCaseMember;
import gr.aegean.icsd.icarus.user.IcarusUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@EntityListeners(AuditingEntityListener.class)
public class TestCaseResult {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedBy
    @ManyToOne
    @JoinColumn(updatable = false)
    private IcarusUser creator;

    @NotNull(message = "Test case result's actual response code cannot be null")
    @Positive(message = "Test case result's actual response code cannot be negative")
    private Integer actualResponseCode;

    @NotNull(message = "Test case result's actual response body cannot be null")
    private String actualResponseBody;

    @NotNull(message = "Test case result's pass verdict cannot be null")
    private Boolean pass;

    @ManyToOne(targetEntity = ResourceConfiguration.class, optional = false)
    private ResourceConfiguration resourceConfiguration;

    @ManyToOne(targetEntity = TestCaseMember.class, optional = false)
    private TestCaseMember parentTestCaseMember;



    public TestCaseResult(TestCaseMember parentTestCaseMember, ResourceConfiguration resourceConfiguration,
                          int actualResponseCode, String actualResponseBody, boolean pass) {

        this.parentTestCaseMember = parentTestCaseMember;
        this.resourceConfiguration = resourceConfiguration;
        this.actualResponseCode = actualResponseCode;
        this.actualResponseBody = actualResponseBody;
        this.pass = pass;
    }

    public TestCaseResult() {}



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getActualResponseCode() {
        return actualResponseCode;
    }

    public void setActualResponseCode(Integer actualResponseCode) {
        this.actualResponseCode = actualResponseCode;
    }

    public String getActualResponseBody() {
        return actualResponseBody;
    }

    public void setActualResponseBody(String actualResponseBody) {
        this.actualResponseBody = actualResponseBody;
    }

    public ResourceConfiguration getResourceConfiguration() {
        return resourceConfiguration;
    }

    public void setResourceConfiguration(ResourceConfiguration resourceConfiguration) {
        this.resourceConfiguration = resourceConfiguration;
    }

    public TestCaseMember getParentTestCaseMember() {
        return parentTestCaseMember;
    }

    public void setParentTestCaseMember(TestCaseMember parentTestCaseMember) {
        this.parentTestCaseMember = parentTestCaseMember;
    }

    public IcarusUser getCreator() {
        return creator;
    }

    public void setCreator(IcarusUser creator) {
        this.creator = creator;
    }


}
