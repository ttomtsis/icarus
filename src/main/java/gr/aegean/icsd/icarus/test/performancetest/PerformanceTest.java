package gr.aegean.icsd.icarus.test.performancetest;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.util.enums.Metric;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "functional_test")
public class PerformanceTest extends Test {


    private final Set<Metric> chosenMetrics = new HashSet<>();

    private String pathVariable;

    private String RequestBody;


}
