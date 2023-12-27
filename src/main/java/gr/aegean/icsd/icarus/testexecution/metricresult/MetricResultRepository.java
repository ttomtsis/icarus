package gr.aegean.icsd.icarus.testexecution.metricresult;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricResultRepository  extends JpaRepository<MetricResult, Long> {
}
