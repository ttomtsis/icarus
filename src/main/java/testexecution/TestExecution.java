package testexecution;

import gr.aegean.icsd.icarus.test.performancetest.LoadProfile;
import gr.aegean.icsd.icarus.test.performancetest.ResourceConfiguration;
import jakarta.persistence.*;

@Entity
public class TestExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = LoadProfile.class)
    private LoadProfile loadProfile;

    @ManyToOne(targetEntity = ResourceConfiguration.class)
    private ResourceConfiguration resourceConfiguration;


}
