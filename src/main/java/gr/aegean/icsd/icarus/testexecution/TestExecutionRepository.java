package gr.aegean.icsd.icarus.testexecution;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.user.IcarusUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestExecutionRepository extends JpaRepository<TestExecution, Long> {

    Page<TestExecution> findAllByParentTestAndCreator(Test parentTest, IcarusUser creator, Pageable pageable);

}
