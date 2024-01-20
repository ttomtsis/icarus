package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.function.Function;
import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCase;
import jakarta.persistence.*;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "functional_test")
public class FunctionalTest extends Test {


    @OneToOne(mappedBy = "parentTest", cascade = {CascadeType.REFRESH, CascadeType.REMOVE},
            targetEntity = ResourceConfiguration.class, orphanRemoval = true,
            fetch = FetchType.EAGER)
    private ResourceConfiguration resourceConfiguration;

    @URL(message = "Functional Test's function URL is not a valid URL")
    private String functionURL;

    @OneToMany(mappedBy = "parentTest", cascade = CascadeType.ALL,
            orphanRemoval = true, targetEntity = TestCase.class)
    private final Set<TestCase> testCases = new HashSet<>();



    public static class FunctionalTestBuilder {


        private final String name;
        private final RequestMethod httpMethod;

        private String description;
        private String path;
        private String pathVariable;

        private String functionURL;



        public FunctionalTestBuilder(String name, RequestMethod httpMethod) {
            this.name = name;
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
        super.setHttpMethod(builder.httpMethod);

        super.setDescription(builder.description);
        super.setPath(builder.path);
        super.setPathVariable(builder.pathVariable);

        this.functionURL = builder.functionURL;
    }

    public FunctionalTest() {}

    public static FunctionalTest createFunctionalTestFromModel(FunctionalTestModel model) {

        Function targetFunction = new Function();
        targetFunction.setId(model.getTargetFunction());

        ResourceConfiguration configuration = new ResourceConfiguration();
        configuration.setId(model.getResourceConfiguration());

        return new FunctionalTestBuilder(
                model.getName(),
                model.getHttpMethod())

                .path(model.getPath())
                .pathVariable(model.getPathVariable())

                .functionURL(model.getFunctionUrl())
                .build();
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

    public ResourceConfiguration getResourceConfiguration() {
        return resourceConfiguration;
    }

    public void setResourceConfiguration(ResourceConfiguration resourceConfiguration) {
        this.resourceConfiguration = resourceConfiguration;
    }


}
