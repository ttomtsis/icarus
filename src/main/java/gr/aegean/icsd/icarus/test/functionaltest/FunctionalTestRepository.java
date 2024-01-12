package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FunctionalTestRepository extends JpaRepository<FunctionalTest, Long> {

    Optional<FunctionalTest> findFunctionalTestByIdAndCreator(Long id, IcarusUser creator);

}
