package gr.aegean.icsd.icarus.testexecution;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestExecutionRepository extends JpaRepository<TestExecution, Long> {

    Page<TestExecution> findAllByParentTestAndCreator
            (Test parentTest, IcarusUser creator, Pageable pageable);

    Optional<TestExecution> findTestExecutionByIdAndParentTestAndCreator
            (Long id, Test test, IcarusUser creator);

    Optional<TestExecution> findTestExecutionByDeploymentIdAndParentTestAndCreator
            (String deploymentId, Test test, IcarusUser creator);

    Optional<TestExecution> findTestExecutionByParentTestAndDeploymentIdAndCreator
            (Test parentTest, String deploymentId, IcarusUser creator);

}
