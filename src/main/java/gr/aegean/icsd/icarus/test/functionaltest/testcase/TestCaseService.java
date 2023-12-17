package gr.aegean.icsd.icarus.test.functionaltest.testcase;

import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.test.functionaltest.FunctionalTest;
import gr.aegean.icsd.icarus.util.exceptions.TestCaseNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.TestNotFoundException;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


@Service
@Transactional
@Validated
public class TestCaseService {

    
    private final TestCaseRepository TestCaseRepository;
    private final TestRepository testRepository;



    public TestCaseService(TestCaseRepository repository, TestRepository testRepository) {
        this.TestCaseRepository = repository;
        this.testRepository = testRepository;
    }



    public Page<TestCase> getTestCases(@NotNull @Positive Long testId,
                                                                 @NotNull Pageable pageable) {

        FunctionalTest parentTest = checkIfTestExists(testId);

        return TestCaseRepository.findAllByParentTest(parentTest, pageable);
    }

    public TestCase createTestCase(@NotNull TestCase newTestCase,
                                                     @NotNull @Positive Long testId) {

        FunctionalTest parentTest = checkIfTestExists(testId);

        newTestCase.setParentTest(parentTest);
        return TestCaseRepository.save(newTestCase);
    }

    public void updateTestCase(Long testId, Long TestCaseId, TestCaseModel model) {

        checkIfTestExists(testId);

        TestCase existingTestCase = checkIfTestCaseExists(TestCaseId);

        if (StringUtils.isNotBlank(model.getName())) {
            existingTestCase.setName(model.getName());
        }

        if (StringUtils.isNotBlank(model.getDescription())) {
            existingTestCase.setDescription(model.getDescription());
        }

        TestCaseRepository.save(existingTestCase);
    }

    public void deleteTestCase(Long testId, Long TestCaseId) {

        checkIfTestExists(testId);

        TestCase existingTestCase = checkIfTestCaseExists(TestCaseId);

        TestCaseRepository.delete(existingTestCase);
    }


    private FunctionalTest checkIfTestExists(Long parentTestId) {

        return (FunctionalTest) testRepository.findById(parentTestId)
                .orElseThrow( () -> new TestNotFoundException(parentTestId));
    }

    private TestCase checkIfTestCaseExists(Long TestCaseId) {

        return TestCaseRepository.findById(TestCaseId)
                .orElseThrow( () -> new TestCaseNotFoundException(TestCaseId));
    }

}
