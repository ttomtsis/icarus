package gr.aegean.icsd.icarus.test.functionaltest;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;


@Entity
public class TestCase {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = FunctionalTest.class)
    private FunctionalTest parentTest;

    @OneToMany(mappedBy = "parentTestCase", cascade = CascadeType.ALL, targetEntity = TestCaseMember.class)
    private final Set<TestCaseMember> testCaseMembers = new HashSet<>();



    public TestCase(FunctionalTest parentTest, Set<TestCaseMember> testCaseMembers) {
        this.parentTest = parentTest;
        this.testCaseMembers.addAll(testCaseMembers);
    }

    public TestCase(FunctionalTest parentTest, TestCaseMember testCaseMember) {
        this.parentTest = parentTest;
        this.testCaseMembers.add(testCaseMember);
    }

    public TestCase(FunctionalTest parentTest) {
        this.parentTest = parentTest;
    }

    public TestCase() {}



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
