package gr.aegean.icsd.icarus.test.functionaltest.testcase;

import gr.aegean.icsd.icarus.test.functionaltest.FunctionalTest;
import gr.aegean.icsd.icarus.test.functionaltest.testcasemember.TestCaseMember;
import gr.aegean.icsd.icarus.user.IcarusUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashSet;
import java.util.Set;

import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.*;


@Entity
@EntityListeners(AuditingEntityListener.class)
public class TestCase {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedBy
    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(updatable = false)
    private IcarusUser creator;

    @NotBlank(message = "Test case name cannot be blank")
    @Size(min = MIN_LENGTH, max = MAX_LENGTH, message = "Test case name does not conform to length limitations")
    @Column(unique = true)
    private String name;

    @Size(min = MIN_LENGTH, max = MAX_DESCRIPTION_LENGTH,
            message = "Test case description does not conform to length limitations")
    private String description;

    @ManyToOne(targetEntity = FunctionalTest.class, optional = false)
    private FunctionalTest parentTest;

    @OneToMany(mappedBy = "parentTestCase", cascade = CascadeType.ALL,
            orphanRemoval = true, targetEntity = TestCaseMember.class)
    private final Set<TestCaseMember> testCaseMembers = new HashSet<>();



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

    public IcarusUser getCreator() {
        return creator;
    }

    public void setCreator(IcarusUser creator) {
        this.creator = creator;
    }


}
