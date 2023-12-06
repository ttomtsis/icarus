package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.test.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
}
