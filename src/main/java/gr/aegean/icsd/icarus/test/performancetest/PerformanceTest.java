package gr.aegean.icsd.icarus.test.performancetest;

import gr.aegean.icsd.icarus.function.Function;
import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.user.IcarusUser;
import gr.aegean.icsd.icarus.util.enums.Metric;
import jakarta.persistence.*;
import org.springframework.http.HttpMethod;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "performance_test")
public class PerformanceTest extends Test  {


    @ElementCollection(targetClass = Metric.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "chosen_metrics")
    @Column(name = "metric")
    private final Set<Metric> chosenMetrics = new HashSet<>();

    private String pathVariable;

    private String pathVariableValue;

    private String requestBody;



    public static class PerformanceTestBuilder {


        private final String name;
        private final IcarusUser testAuthor;
        private final Function targetFunction;
        private final HttpMethod httpMethod;

        private String description;
        private String path;
        private String pathVariable;

        private String pathVariableValue;
        private String requestBody;


        public PerformanceTestBuilder(String name, IcarusUser author, Function targetFunction,
                                      HttpMethod httpMethod) {

            this.name = name;
            this.testAuthor = author;
            this.targetFunction = targetFunction;
            this.httpMethod = httpMethod;

        }


        public PerformanceTestBuilder pathVariableValue (String pathVariableValue) {
            this.pathVariableValue = pathVariableValue;
            return this;
        }

        public PerformanceTestBuilder requestBody (String requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public PerformanceTest build () {
            return new PerformanceTest(this);
        }


    }



    public PerformanceTest() {}

    private PerformanceTest(PerformanceTestBuilder builder) {
        super.setName(builder.name);
        super.setAuthor(builder.testAuthor);
        super.setTargetFunction(builder.targetFunction);
        super.setHttpMethod(builder.httpMethod);

        super.setDescription(builder.description);
        super.setPath(builder.path);
        super.setPathVariable(builder.pathVariable);

        this.pathVariableValue = builder.pathVariable;
        this.requestBody = builder.requestBody;
    }



    public String getPathVariable() {
        return super.getPathVariable();
    }

    public String getPath () {
        return super.getPath();
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public Set<Metric> getChosenMetrics() {
        return chosenMetrics;
    }

    public void addMetric(Metric newMetric) {
        this.chosenMetrics.add(newMetric);
    }


}
