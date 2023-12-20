package gr.aegean.icsd.icarus.testexecution;

import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.test.functionaltest.testcasemember.TestCaseMember;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


@Entity
public class TestCaseResult {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Test case result's actual response code cannot be null")
    @Positive(message = "Test case result's actual response code cannot be negative")
    private Integer actualResponseCode;

    @NotNull(message = "Test case result's actual response body cannot be null")
    private String actualResponseBody;

    @NotNull(message = "Test case result's pass verdict cannot be null")
    private Boolean pass;

    @NotBlank
    private String functionUrl;

    @ManyToOne(targetEntity = ResourceConfiguration.class, optional = false)
    private ResourceConfiguration resourceConfiguration;

    @ManyToOne(targetEntity = TestCaseMember.class, optional = false)
    private TestCaseMember parentTestCaseMember;



    public TestCaseResult(TestCaseMember parentTestCaseMember, ResourceConfiguration resourceConfiguration,
                          int actualResponseCode, String actualResponseBody, boolean pass, String functionUrl) {

        this.parentTestCaseMember = parentTestCaseMember;
        this.resourceConfiguration = resourceConfiguration;
        this.actualResponseCode = actualResponseCode;
        this.actualResponseBody = actualResponseBody;
        this.pass = pass;
        this.functionUrl = functionUrl;
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


}
