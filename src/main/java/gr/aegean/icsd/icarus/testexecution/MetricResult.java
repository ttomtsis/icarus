package gr.aegean.icsd.icarus.testexecution;

import gr.aegean.icsd.icarus.test.performancetest.loadprofile.LoadProfile;
import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Entity
public class MetricResult {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String functionUrl;

    @NotBlank
    private String metricName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "metric_result_metric_values", joinColumns = @JoinColumn(name = "id"))
    private HashMap<String, String> metricValues;

    @CollectionTable(name = "metric_result_load_profiles", joinColumns = @JoinColumn(name = "id"))
    @ManyToMany(targetEntity = LoadProfile.class)
    private Set<LoadProfile> loadProfiles;

    @ManyToOne(targetEntity = ResourceConfiguration.class, optional = false)
    private ResourceConfiguration resourceConfiguration;



    public MetricResult(Set<LoadProfile> profiles, ResourceConfiguration resourceConfiguration,
                        String functionUrl, Map<String, String> values, String metricName) {

        this.loadProfiles = profiles;
        this.resourceConfiguration = resourceConfiguration;
        this.functionUrl = functionUrl;
        this.metricValues = (HashMap<String, String>) values;
        this.metricName = metricName;
    }

    public MetricResult() {}



    public void setId(Long id) {
        this.id = id;
    }

    public void setLoadProfiles(Set<LoadProfile> loadProfiles) {
        this.loadProfiles = loadProfiles;
    }

    public void setResourceConfiguration(ResourceConfiguration resourceConfiguration) {
        this.resourceConfiguration = resourceConfiguration;
    }

    public Long getId() {
        return id;
    }

    public Set<LoadProfile> getLoadProfiles() {
        return loadProfiles;
    }

    public ResourceConfiguration getResourceConfiguration() {
        return resourceConfiguration;
    }

    public String getFunctionUrl() {
        return functionUrl;
    }

    public void setFunctionUrl(String functionUrl) {
        this.functionUrl = functionUrl;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public HashMap<String, String> getMetricValues() {
        return metricValues;
    }

    public void setMetricValues(HashMap<String, String> metricValues) {
        this.metricValues = metricValues;
    }


}

