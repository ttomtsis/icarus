package testexecution;

import gr.aegean.icsd.icarus.test.performancetest.LoadProfile;
import gr.aegean.icsd.icarus.test.performancetest.ResourceConfiguration;
import jakarta.persistence.*;

@Entity
public class MetricResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = LoadProfile.class, optional = false)
    private LoadProfile loadProfile;

    @ManyToOne(targetEntity = ResourceConfiguration.class, optional = false)
    private ResourceConfiguration resourceConfiguration;



    public MetricResult(LoadProfile loadProfile, ResourceConfiguration resourceConfiguration) {
        this.loadProfile = loadProfile;
        this.resourceConfiguration = resourceConfiguration;
    }

    public MetricResult() {}


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

