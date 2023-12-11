package gr.aegean.icsd.icarus.test.performancetest;

import gr.aegean.icsd.icarus.function.Function;
import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.performancetest.loadprofile.LoadProfile;
import gr.aegean.icsd.icarus.test.performancetest.resourceconfiguration.ResourceConfiguration;
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

    private String pathVariableValue;

    private String requestBody;

    @OneToMany(mappedBy = "parentTest", cascade = CascadeType.ALL,
            targetEntity = LoadProfile.class, orphanRemoval = true)
    private final Set<LoadProfile> loadProfiles = new HashSet<>();

    @OneToMany(mappedBy = "parentTest", cascade = CascadeType.ALL,
            targetEntity = LoadProfile.class, orphanRemoval = true)
    private final Set<ResourceConfiguration> resourceConfigurations = new HashSet<>();



    public static class PerformanceTestBuilder {


        private final String name;
        private final IcarusUser testAuthor;
        private final Function targetFunction;
        private final HttpMethod httpMethod;
        private final Set<Metric> chosenMetrics = new HashSet<>();

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

        public PerformanceTestBuilder metrics (Metric newMetric) {
            this.chosenMetrics.add(newMetric);
            return this;
        }

        public PerformanceTestBuilder metrics (Set<Metric> newMetrics) {
            this.chosenMetrics.addAll(newMetrics);
            return this;
        }

        public PerformanceTest build () {
            return new PerformanceTest(this);
        }


    }



    public PerformanceTest() {}

    public static PerformanceTest createPerformanceTestFromModel(PerformanceTestModel model) {

        IcarusUser author = new IcarusUser();
        author.setId(model.getTestAuthor());

        Function targetFunction = new Function();
        targetFunction.setId(model.getTargetFunction());

        return new PerformanceTestBuilder(
                model.getName(), author, targetFunction,
                HttpMethod.valueOf(model.getHttpMethod())
        )
                .pathVariableValue(model.getPathVariableValue())
                .requestBody(model.getRequestBody())
                .metrics(model.getChosenMetrics())
                .build();

    }

    private PerformanceTest(PerformanceTestBuilder builder) {
        super.setName(builder.name);
        super.setAuthor(builder.testAuthor);
        super.setTargetFunction(builder.targetFunction);
        super.setHttpMethod(builder.httpMethod);

        super.setDescription(builder.description);
        super.setPath(builder.path);
        super.setPathVariable(builder.pathVariable);

        this.chosenMetrics.addAll(builder.chosenMetrics);

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

    public void setChosenMetrics(Set<Metric> newMetrics) {
        this.chosenMetrics.clear();
        this.chosenMetrics.addAll(newMetrics);
    }

    public void addMetric(Metric newMetric) {
        this.chosenMetrics.add(newMetric);
    }

    public void removeMetric(Metric metric) {this.chosenMetrics.remove(metric);}

    public String getPathVariableValue() {
        return pathVariableValue;
    }

    public void setPathVariableValue(String pathVariableValue) {
        this.pathVariableValue = pathVariableValue;
    }

    public Set<LoadProfile> getLoadProfiles() {
        return loadProfiles;
    }

    public Set<ResourceConfiguration> getResourceConfigurations() {
        return resourceConfigurations;
    }


}
