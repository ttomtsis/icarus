package gr.aegean.icsd.icarus.test.functionaltest.testcasemember;

import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TestCaseMemberRepository extends JpaRepository<TestCaseMember, Long> {

    Page<TestCaseMember> findAllByParentTestCase(TestCase parentTestCase, Pageable pageable);


}
