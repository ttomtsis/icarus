package gr.aegean.icsd.icarus.testexecution.metricresult;

import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.test.performancetest.loadprofile.LoadProfile;
import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class MetricResult {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedBy
    @ManyToOne
    @JoinColumn(updatable = false)
    private IcarusUser creator;

    @NotBlank(message = "Metric result's metric name cannot be blank")
    private String metricName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "metric_result_metric_values", joinColumns = @JoinColumn(name = "id"))
    private final Map<String, String> metricValues = new HashMap<>();

    @ManyToMany(targetEntity = LoadProfile.class)
    private final Set<LoadProfile> loadProfiles = new HashSet<>();

    @ManyToOne(targetEntity = ResourceConfiguration.class, optional = false)
    @JoinColumn(name = "configuration_used", updatable = false)
    private ResourceConfiguration resourceConfiguration;



    public MetricResult(Set<LoadProfile> profiles, ResourceConfiguration resourceConfiguration,
                        Map<String, String> values, String metricName, IcarusUser creator) {

        this.loadProfiles.addAll(profiles);
        this.resourceConfiguration = resourceConfiguration;
        this.metricValues.putAll(values);
        this.metricName = metricName;
        this.creator = creator;
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

    public IcarusUser getCreator() {
        return creator;
    }

    public void setCreator(IcarusUser creator) {
        this.creator = creator;
    }


}

