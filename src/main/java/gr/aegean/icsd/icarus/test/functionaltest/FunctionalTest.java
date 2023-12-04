package gr.aegean.icsd.icarus.test.functionaltest;


import gr.aegean.icsd.icarus.function.Function;
import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.performancetest.PerformanceTest;
import gr.aegean.icsd.icarus.user.IcarusUser;
import gr.aegean.icsd.icarus.util.aws.AwsRegion;
import gr.aegean.icsd.icarus.util.gcp.GcpRegion;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.URL;
import org.springframework.http.HttpMethod;

@Entity
@Table(name = "functional_test")
public class FunctionalTest extends Test {


    @NotBlank(message = "Function URL cannot be blank")
    @URL(message = "Function URL is not a valid URL")
    private String functionURL;

    @NotNull(message = "Allocated memory cannot be null")
    @Positive(message = "Allocated memory must be positive")
    @Min(value = 128, message = "Minimum allocated memory must be at least 128MB")
    private Integer usedMemory;

    @NotBlank(message = "Region cannot be blank")
    private String region;



    public static class FunctionalTestBuilder extends TestBuilder {


        private final String name;
        private final IcarusUser testAuthor;
        private final Function targetFunction;
        private final HttpMethod httpMethod;

        private String description;
        private String path;
        private String pathVariable;

        private String functionURL;
        private Integer usedMemory;
        private String region;



        public FunctionalTestBuilder(String name, IcarusUser author, Function targetFunction,
                                      HttpMethod httpMethod) {
            super(name, author, targetFunction, httpMethod);
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


}
