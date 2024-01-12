package gr.aegean.icsd.icarus.test;

import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {

    Optional<Test> findTestByIdAndCreator(Long id, IcarusUser creator);
}
