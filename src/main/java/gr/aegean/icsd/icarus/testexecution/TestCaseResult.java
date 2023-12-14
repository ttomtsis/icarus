package gr.aegean.icsd.icarus.testexecution;

import gr.aegean.icsd.icarus.test.functionaltest.testcasemember.TestCaseMember;
import gr.aegean.icsd.icarus.test.resourceconfiguration.ResourceConfiguration;
import jakarta.persistence.*;


@Entity
public class TestCaseResult {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = ResourceConfiguration.class, optional = false)
    private ResourceConfiguration resourceConfiguration;

    @ManyToOne(targetEntity = TestCaseMember.class, optional = false)
    private TestCaseMember parentTestCaseMember;



    public TestCaseResult(TestCaseMember parentTestCaseMember, ResourceConfiguration resourceConfiguration) {
        this.parentTestCaseMember = parentTestCaseMember;
        this.resourceConfiguration = resourceConfiguration;
    }

    public TestCaseResult() {}


    public void setId(Long id) {
        this.id = id;
    }

    public ResourceConfiguration getResourceConfiguration() {
        return resourceConfiguration;
    }

    public void setResourceConfiguration(ResourceConfiguration resourceConfiguration) {
        this.resourceConfiguration = resourceConfiguration;
    }

    public void setParentTestCaseMember(TestCaseMember parentTestCaseMember) {
        this.parentTestCaseMember = parentTestCaseMember;
    }

    public Long getId() {
        return id;
    }

    public TestCaseMember getParentTestCaseMember() {
        return parentTestCaseMember;
    }


}
