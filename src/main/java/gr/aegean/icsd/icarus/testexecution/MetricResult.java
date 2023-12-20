package gr.aegean.icsd.icarus.testexecution;

import gr.aegean.icsd.icarus.test.performancetest.loadprofile.LoadProfile;
import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
public class MetricResult {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String functionUrl;

    @ManyToOne(targetEntity = LoadProfile.class, optional = false)
    private LoadProfile loadProfile;

    @ManyToOne(targetEntity = ResourceConfiguration.class, optional = false)
    private ResourceConfiguration resourceConfiguration;



    public MetricResult(LoadProfile loadProfile, ResourceConfiguration resourceConfiguration, String functionUrl) {
        this.loadProfile = loadProfile;
        this.resourceConfiguration = resourceConfiguration;
        this.functionUrl = functionUrl;
    }

    public MetricResult() {}



    public void setId(Long id) {
        this.id = id;
    }

    public void setLoadProfile(LoadProfile loadProfile) {
        this.loadProfile = loadProfile;
    }

    public void setResourceConfiguration(ResourceConfiguration resourceConfiguration) {
        this.resourceConfiguration = resourceConfiguration;
    }

    public Long getId() {
        return id;
    }

    public LoadProfile getLoadProfile() {
        return loadProfile;
    }

    public ResourceConfiguration getResourceConfiguration() {
        return resourceConfiguration;
    }


}

