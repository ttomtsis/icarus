package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.function.Function;
import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCase;
import gr.aegean.icsd.icarus.test.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.user.IcarusUser;
import gr.aegean.icsd.icarus.util.aws.AwsRegion;
import gr.aegean.icsd.icarus.util.enums.Platform;
import gr.aegean.icsd.icarus.util.exceptions.InvalidTestConfigurationException;
import gr.aegean.icsd.icarus.util.gcp.GcpRegion;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @OneToMany(mappedBy = "parentTest", cascade = CascadeType.ALL,
            targetEntity = ResourceConfiguration.class, orphanRemoval = true)
    @Size(max = 2, message = "Functional Tests can consist only of two resource configurations")
    private final Set<ResourceConfiguration> resourceConfigurations = new HashSet<>();



    public static class FunctionalTestBuilder {


        private final String name;
        private final IcarusUser testAuthor;
        private final HttpMethod httpMethod;

        private String description;
        private String path;
        private String pathVariable;

        private String functionURL;
        private Integer usedMemory;
        private String region;



        public FunctionalTestBuilder(String name, IcarusUser author, HttpMethod httpMethod) {
            this.name = name;
            this.testAuthor = author;
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

        public FunctionalTestBuilder region (String region) {
            this.region = region;
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

        public FunctionalTest build () {
            return new FunctionalTest(this);
        }


    }


    private FunctionalTest(FunctionalTestBuilder builder) {

        super.setName(builder.name);
        super.setAuthor(builder.testAuthor);
        super.setHttpMethod(builder.httpMethod);

        super.setDescription(builder.description);
        super.setPath(builder.path);
        super.setPathVariable(builder.pathVariable);

        this.functionURL = builder.functionURL;
        this.usedMemory = builder.usedMemory;
        this.region = builder.region;
    }

    public FunctionalTest() {}

    public static FunctionalTest createFunctionalTestFromModel(FunctionalTestModel model) {

        IcarusUser author = new IcarusUser();
        author.setId(model.getTestAuthor());

        Function targetFunction = new Function();
        targetFunction.setId(model.getTargetFunction());


        return new FunctionalTestBuilder(
                model.getName(), author,
                HttpMethod.valueOf(model.getHttpMethod()))

                .pathVariable(model.getPathVariable())

                .region(model.getRegion())
                .usedMemory(model.getUsedMemory())
                .functionURL(model.getFunctionUrl())
                .build();
    }


    @PrePersist
    private void checkConfigurations() {

        int totalAwsConfigurations = 0;
        int totalGcpConfigurations = 0;

        for (ResourceConfiguration configuration : this.resourceConfigurations) {
            if (configuration.getProviderPlatform().equals(Platform.AWS)) {
                totalAwsConfigurations++;
            }
            if (configuration.getProviderPlatform().equals(Platform.GCP)) {
                totalGcpConfigurations++;
            }
        }

        if (totalAwsConfigurations > 1 || totalGcpConfigurations > 1) {
            throw new InvalidTestConfigurationException("A Functional Test may only contain one type of " +
                    "resource configuration per platform");
        }
    }


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

    public void setRegion(String newRegion) {
        this.region = newRegion;
    }

    public Set<TestCase> getTestCases() {
        return testCases;
    }

    public Set<ResourceConfiguration> getResourceConfigurations() {
        return resourceConfigurations;
    }


}
