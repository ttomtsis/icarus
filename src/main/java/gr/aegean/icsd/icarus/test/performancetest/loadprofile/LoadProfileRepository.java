package gr.aegean.icsd.icarus.test.performancetest.loadprofile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LoadProfileRepository extends JpaRepository<LoadProfile, Long> {
}
