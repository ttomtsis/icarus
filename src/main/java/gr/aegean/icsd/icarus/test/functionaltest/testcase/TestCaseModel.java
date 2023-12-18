package gr.aegean.icsd.icarus.test.functionaltest.testcase;

import gr.aegean.icsd.icarus.test.functionaltest.testcasemember.TestCaseMemberModel;
import org.springframework.hateoas.RepresentationModel;

import java.util.HashSet;
import java.util.Set;


public class TestCaseModel extends RepresentationModel<TestCaseModel> {


    private Long id;
    private String name;
    private String description;
    private Long parentTest;
    private Set<TestCaseMemberModel> testCaseMembers = new HashSet<>();




    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getParentTest() {
        return parentTest;
    }

    public void setParentTest(Long parentTest) {
        this.parentTest = parentTest;
    }

    public Set<TestCaseMemberModel> getTestCaseMembers() {
        return testCaseMembers;
    }

    public void setTestCaseMembers(Set<TestCaseMemberModel> testCaseMembers) {
        this.testCaseMembers = testCaseMembers;
    }


}
