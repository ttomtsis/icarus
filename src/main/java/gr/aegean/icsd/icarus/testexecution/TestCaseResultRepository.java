package gr.aegean.icsd.icarus.testexecution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestCaseResultRepository extends JpaRepository<TestCaseResult, Long> {
}
