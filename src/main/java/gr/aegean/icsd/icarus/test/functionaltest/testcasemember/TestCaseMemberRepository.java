package gr.aegean.icsd.icarus.test.functionaltest.testcasemember;

import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCase;
import gr.aegean.icsd.icarus.user.IcarusUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface TestCaseMemberRepository extends JpaRepository<TestCaseMember, Long> {

    Page<TestCaseMember> findAllByParentTestCaseAndCreator(TestCase parentTestCase, IcarusUser creator,
                                                           Pageable pageable);

    Optional<TestCaseMember> findByIdAndCreator(Long id, IcarusUser creator);
}
