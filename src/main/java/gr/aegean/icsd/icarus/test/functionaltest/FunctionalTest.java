package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.function.Function;
import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.user.IcarusUser;
import gr.aegean.icsd.icarus.util.aws.AwsRegion;
import gr.aegean.icsd.icarus.util.gcp.GcpRegion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.URL;
import org.springframework.http.HttpMethod;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "functional_test")
public class FunctionalTest extends Test {


    @NotBlank(message = "Functional Test's function URL cannot be blank")
    @URL(message = "Functional Test's function URL is not a valid URL")
    private String functionURL;

    @NotNull(message = "Functional Test's allocated memory cannot be null")
    @Positive(message = "Functional Test's allocated memory must be positive")
    @Min(value = 128, message = "Functional Test's minimum allocated memory must be at least 128MB")
    private Integer usedMemory;

    @NotBlank(message = "Functional Test's region cannot be blank")
    private String region;

    @OneToMany(mappedBy = "parentTest", cascade = CascadeType.ALL,
            orphanRemoval = true, targetEntity = TestCase.class)
    private final Set<TestCase> testCases = new HashSet<>();



    public static class FunctionalTestBuilder {


        private final String name;
        private final IcarusUser testAuthor;
        private final Function targetFunction;
        private final HttpMethod httpMethod;
        private final Set<TestCase> testCases = new HashSet<>();


        private String description;
        private String path;
        private String pathVariable;

        private String functionURL;
        private Integer usedMemory;
        private String region;



        public FunctionalTestBuilder(String name, IcarusUser author, Function targetFunction,
                                      HttpMethod httpMethod) {
            this.name = name;
            this.testAuthor = author;
            this.targetFunction = targetFunction;
            this.httpMethod = httpMethod;
        }

        public FunctionalTestBuilder description (String description) {
            this.description = description;
            return this;
        }

        public FunctionalTestBuilder path (String path) {
            this.path = path;
            return this;
        }

        public FunctionalTestBuilder pathVariable (String pathVariable) {
            this.pathVariable = pathVariable;
            return this;
        }

        public FunctionalTestBuilder functionURL (String functionURL) {
            this.functionURL = functionURL;
            return this;
        }

        public FunctionalTestBuilder usedMemory (Integer usedMemory) {
            this.usedMemory = usedMemory;
            return this;
        }

        public FunctionalTestBuilder region (AwsRegion region) {
            this.region = region.get();
            return this;
        }

        public FunctionalTestBuilder region (GcpRegion region) {
            this.region = region.get();
            return this;
        }

        public FunctionalTestBuilder testCase (TestCase testCase) {
            this.testCases.add(testCase);
            return this;
        }

        public FunctionalTestBuilder testCase (Set<TestCase> testCases) {
            this.testCases.addAll(testCases);
            return this;
        }

        public FunctionalTest build () {
            return new FunctionalTest(this);
        }


    }



    private FunctionalTest(FunctionalTestBuilder builder) {
        super.setName(builder.name);
        super.setAuthor(builder.testAuthor);
        super.setTargetFunction(builder.targetFunction);
        super.setHttpMethod(builder.httpMethod);

        super.setDescription(builder.description);
        super.setPath(builder.path);
        super.setPathVariable(builder.pathVariable);

        this.testCases.addAll(builder.testCases);

        this.functionURL = builder.functionURL;
        this.usedMemory = builder.usedMemory;
        this.region = builder.region;
    }

    public FunctionalTest() {}



    public String getFunctionURL() {
        return functionURL;
    }

    public void setFunctionURL(String functionURL) {
        this.functionURL = functionURL;
    }

    public Integer getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(Integer usedMemory) {
        this.usedMemory = usedMemory;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Set<TestCase> getTestCases() {
        return testCases;
    }


    public void addTestCase(TestCase newTestCase) {
        this.testCases.add(newTestCase);
    }

    public void addTestCase(Set<TestCase> newTestCases) {
        this.testCases.addAll(newTestCases);
    }

    public void removeTestCase(TestCase testCase) {
        this.testCases.remove(testCase);
    }

    public void removeTestCase(Set<TestCase> testCases) {
        this.testCases.removeAll(testCases);
    }


}
