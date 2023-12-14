package gr.aegean.icsd.icarus.test.functionaltest.testcase;

import gr.aegean.icsd.icarus.test.functionaltest.FunctionalTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

    Page<TestCase> findAllByParentTest(FunctionalTest parentTest, Pageable pageable);

}
