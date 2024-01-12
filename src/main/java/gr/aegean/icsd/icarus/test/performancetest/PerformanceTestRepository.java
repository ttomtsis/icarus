package gr.aegean.icsd.icarus.test.performancetest;

import gr.aegean.icsd.icarus.user.IcarusUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerformanceTestRepository extends JpaRepository<PerformanceTest, Long> {

    Optional<PerformanceTest> findPerformanceTestByIdAndCreator(Long id, IcarusUser creator);

}
