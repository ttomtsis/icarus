package gr.aegean.icsd.icarus.testexecution;

import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.test.performancetest.loadprofile.LoadProfile;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
public class MetricResult {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String metricName;

    @NotBlank
    private String deploymentId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "metric_result_metric_values", joinColumns = @JoinColumn(name = "id"))
    private final Map<String, String> metricValues = new HashMap<>();

    @ManyToMany(targetEntity = LoadProfile.class)
    private final Set<LoadProfile> loadProfiles = new HashSet<>();

    @ManyToOne(targetEntity = ResourceConfiguration.class, optional = false)
    @JoinColumn(name = "configuration_used", updatable = false)
    private ResourceConfiguration resourceConfiguration;



    public MetricResult(Set<LoadProfile> profiles, ResourceConfiguration resourceConfiguration,
                        Map<String, String> values, String metricName, String deploymentId) {

        this.loadProfiles.addAll(profiles);
        this.resourceConfiguration = resourceConfiguration;
        this.metricValues.putAll(values);
        this.metricName = metricName;
        this.deploymentId = deploymentId;
    }

    public MetricResult() {}



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public Map<String, String> getMetricValues() {
        return metricValues;
    }

    public Set<LoadProfile> getLoadProfiles() {
        return loadProfiles;
    }

    public ResourceConfiguration getResourceConfiguration() {
        return resourceConfiguration;
    }

    public void setResourceConfiguration(ResourceConfiguration resourceConfiguration) {
        this.resourceConfiguration = resourceConfiguration;
    }


}

