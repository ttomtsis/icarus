package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.function.Function;
import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCase;
import gr.aegean.icsd.icarus.test.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.user.IcarusUser;
import gr.aegean.icsd.icarus.util.enums.Platform;
import gr.aegean.icsd.icarus.util.exceptions.InvalidTestConfigurationException;
import jakarta.persistence.*;
import org.hibernate.validator.constraints.URL;
import org.springframework.http.HttpMethod;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "functional_test")
public class FunctionalTest extends Test {


    @URL(message = "Functional Test's function URL is not a valid URL")
    private String functionURL;

    @OneToMany(mappedBy = "parentTest", cascade = CascadeType.ALL,
            orphanRemoval = true, targetEntity = TestCase.class)
    private final Set<TestCase> testCases = new HashSet<>();



    public static class FunctionalTestBuilder {


        private final String name;
        private final IcarusUser testAuthor;
        private final HttpMethod httpMethod;

        private String description;
        private String path;
        private String pathVariable;

        private String functionURL;



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

                .functionURL(model.getFunctionUrl())
                .build();
    }


    @PrePersist
    private void checkConfigurations() {

        int totalAwsConfigurations = 0;
        int totalGcpConfigurations = 0;

        for (ResourceConfiguration configuration : this.getResourceConfigurations()) {
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

    public Set<TestCase> getTestCases() {
        return testCases;
    }


}
