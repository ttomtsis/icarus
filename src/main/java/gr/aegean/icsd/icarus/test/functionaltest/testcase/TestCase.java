package gr.aegean.icsd.icarus.test.functionaltest.testcase;

import gr.aegean.icsd.icarus.test.functionaltest.FunctionalTest;
import gr.aegean.icsd.icarus.test.functionaltest.testcasemember.TestCaseMember;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.*;


@Entity
public class TestCase {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Test case name cannot be blank")
    @Size(min = minLength, max = maxLength, message = "Test case name does not conform to length limitations")
    @Column(unique = true)
    private String name;

    @Size(min = minLength, max = maxDescriptionLength,
            message = "Test case description does not conform to length limitations")
    private String description;

    @ManyToOne(targetEntity = FunctionalTest.class, optional = false)
    private FunctionalTest parentTest;

    @OneToMany(mappedBy = "parentTestCase", cascade = CascadeType.ALL,
            orphanRemoval = true, targetEntity = TestCaseMember.class)
    private final Set<TestCaseMember> testCaseMembers = new HashSet<>();



    public TestCase(String name, String description, FunctionalTest parentTest, Set<TestCaseMember> testCaseMembers) {
        this.name = name;
        this.description = description;
        this.parentTest = parentTest;
        this.testCaseMembers.addAll(testCaseMembers);
    }

    public TestCase(String name, String description, FunctionalTest parentTest) {
        this.name = name;
        this.description = description;
        this.parentTest = parentTest;
    }

    public TestCase() {}

    public static TestCase createTestCaseFromModel(TestCaseModel model) {

        FunctionalTest parentTest = new FunctionalTest();
        parentTest.setId(model.getParentTest());

        return new TestCase(model.getName(), model.getDescription(), parentTest);
    }



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

    public Set<TestCaseMember> getTestCaseMembers() {
        return testCaseMembers;
    }

    public FunctionalTest getParentTest() {
        return parentTest;
    }

    public void setParentTest(FunctionalTest parentTest) {
        this.parentTest = parentTest;
    }

    public void addMember(TestCaseMember newMember) {
        this.testCaseMembers.add(newMember);
    }

    public void addMember(Set<TestCaseMember> newMembers) {
        this.testCaseMembers.addAll(newMembers);
    }

    public void removeMember(TestCaseMember member) {
        this.testCaseMembers.remove(member);
    }

    public void removeMember(Set<TestCaseMember> members) {
        this.testCaseMembers.removeAll(members);
    }


}
