package gr.aegean.icsd.icarus.test.functionaltest.testcase;

import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.test.functionaltest.FunctionalTest;
import gr.aegean.icsd.icarus.util.exceptions.TestCaseNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.test.TestNotFoundException;
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

    
    private final TestCaseRepository testCaseRepository;
    private final TestRepository testRepository;



    public TestCaseService(TestCaseRepository repository, TestRepository testRepository) {
        this.testCaseRepository = repository;
        this.testRepository = testRepository;
    }



    public Page<TestCase> getTestCases(@NotNull @Positive Long testId, @NotNull Pageable pageable) {

        FunctionalTest parentTest = checkIfTestExists(testId);

        return testCaseRepository.findAllByParentTest(parentTest, pageable);
    }

    public TestCase createTestCase(@NotNull TestCase newTestCase, @NotNull @Positive Long testId) {

        FunctionalTest parentTest = checkIfTestExists(testId);

        newTestCase.setParentTest(parentTest);
        return testCaseRepository.save(newTestCase);
    }

    public void updateTestCase(@NotNull @Positive Long testId, @NotNull @Positive Long testCaseId,
                               @NotNull TestCaseModel model) {

        checkIfTestExists(testId);

        TestCase existingTestCase = checkIfTestCaseExists(testCaseId);

        if (StringUtils.isNotBlank(model.getName())) {
            existingTestCase.setName(model.getName());
        }

        if (StringUtils.isNotBlank(model.getDescription())) {
            existingTestCase.setDescription(model.getDescription());
        }

        testCaseRepository.save(existingTestCase);
    }

    public void deleteTestCase(@NotNull @Positive Long testId, @NotNull @Positive Long testCaseId) {

        checkIfTestExists(testId);

        TestCase existingTestCase = checkIfTestCaseExists(testCaseId);

        testCaseRepository.delete(existingTestCase);
    }


    private FunctionalTest checkIfTestExists(@NotNull @Positive Long parentTestId) {

        return (FunctionalTest) testRepository.findById(parentTestId)
                .orElseThrow( () -> new TestNotFoundException(parentTestId));
    }

    private TestCase checkIfTestCaseExists(@NotNull @Positive Long testCaseId) {

        return testCaseRepository.findById(testCaseId)
                .orElseThrow( () -> new TestCaseNotFoundException(testCaseId));
    }


}
