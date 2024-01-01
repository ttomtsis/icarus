package gr.aegean.icsd.icarus.testexecution;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.util.exceptions.TestExecutionNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.test.TestNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


@Service
@Transactional
@Validated
public class TestExecutionService {


    private final TestExecutionRepository repository;
    private final TestRepository testRepository;



    public TestExecutionService(TestExecutionRepository repository, TestRepository testRepository) {
        this.repository = repository;
        this.testRepository = testRepository;
    }



    public Page<TestExecution> getExecutions(Long testId, Pageable pageable) {

        Test parentTest = checkIfTestExists(testId);
        return repository.findAllByParentTest(parentTest, pageable);
    }

    public TestExecution getExecution(Long testId, Long executionId) {

        Test parentTest = checkIfTestExists(testId);

        for (TestExecution execution : parentTest.getTestExecutions()) {
            if (execution.getId().equals(executionId)) {
                return execution;
            }
        }

        throw new TestExecutionNotFoundException(executionId);
    }

    public void deleteExecution(Long testId, Long executionId) {

        Test parentTest = checkIfTestExists(testId);

        for (TestExecution execution : parentTest.getTestExecutions()) {
            if (execution.getId().equals(executionId)) {
                repository.delete(execution);
                return;
            }
        }

        throw new TestExecutionNotFoundException(executionId);
    }


    private Test checkIfTestExists(Long parentTestId) {

        return testRepository.findById(parentTestId)
                .orElseThrow( () -> new TestNotFoundException(parentTestId));
    }


}
