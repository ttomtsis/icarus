package gr.aegean.icsd.icarus.test.performancetest.loadprofile;

import gr.aegean.icsd.icarus.test.performancetest.PerformanceTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LoadProfileRepository extends JpaRepository<LoadProfile, Long> {

    Page<LoadProfile> findAllByParentTest(PerformanceTest parentTest, Pageable pageable);

}
