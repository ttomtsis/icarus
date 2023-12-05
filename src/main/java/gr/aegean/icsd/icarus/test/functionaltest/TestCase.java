package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.test.performancetest.PerformanceTest;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;


@Entity
public class TestCase {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = FunctionalTest.class, optional = false)
    private PerformanceTest parentTest;

    @OneToMany(mappedBy = "parentTestCase", cascade = CascadeType.ALL, targetEntity = TestCaseMember.class)
    private final Set<TestCase> testCaseMembers = new HashSet<>();



    public TestCase(PerformanceTest parentTest) {
        this.parentTest = parentTest;
    }

    public TestCase() {}



    public Long getId() {
        return id;
    }

    public PerformanceTest getParentTest() {
        return parentTest;
    }


}
