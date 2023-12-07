package testexecution;

import gr.aegean.icsd.icarus.test.functionaltest.testcasemember.TestCaseMember;
import jakarta.persistence.*;


@Entity
public class TestCaseResult {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = TestCaseMember.class, optional = false)
    private TestCaseMember parentTestCaseMember;



    public TestCaseResult(TestCaseMember parentTestCaseMember) {
        this.parentTestCaseMember = parentTestCaseMember;
    }

    public TestCaseResult() {}




    public Long getId() {
        return id;
    }

    public TestCaseMember getParentTestCaseMember() {
        return parentTestCaseMember;
    }


}
