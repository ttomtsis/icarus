package gr.aegean.icsd.icarus.test.functionaltest.testcase;

import gr.aegean.icsd.icarus.test.functionaltest.FunctionalTest;
import gr.aegean.icsd.icarus.user.IcarusUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

    Optional<TestCase> findByIdAndCreator(Long id, IcarusUser creator);

    Page<TestCase> findAllByParentTestAndCreator(FunctionalTest parentTest, IcarusUser creator, Pageable pageable);

}
