package gr.aegean.icsd.icarus.test.performancetest.loadprofile;

import gr.aegean.icsd.icarus.test.performancetest.PerformanceTest;
import gr.aegean.icsd.icarus.user.IcarusUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface LoadProfileRepository extends JpaRepository<LoadProfile, Long> {

    Optional<LoadProfile> findByIdAndAndCreator(Long id, IcarusUser creator);
    Page<LoadProfile> findAllByParentTestAndCreator(PerformanceTest parentTest, IcarusUser creator, Pageable pageable);

}
