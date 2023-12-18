package gr.aegean.icsd.icarus.test.functionaltest.testcasemember;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCase;
import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCaseRepository;
import gr.aegean.icsd.icarus.util.exceptions.testcasemember.InvalidTestCaseMemberConfigurationException;
import gr.aegean.icsd.icarus.util.exceptions.testcasemember.TestCaseMemberNotFoundException;
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

import java.util.function.Consumer;


@Service
@Transactional
@Validated
public class TestCaseMemberService {
    
    
    private final TestCaseMemberRepository testCaseMemberRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestRepository testRepository;



    public TestCaseMemberService(TestCaseMemberRepository testCaseMemberRepository,
                                 TestCaseRepository testCaseRepository, TestRepository testRepository) {
        
        this.testCaseMemberRepository = testCaseMemberRepository;
        this.testCaseRepository = testCaseRepository;
        this.testRepository = testRepository;
    }

    
    
    public Page<TestCaseMember> getTestCaseMembers(@NotNull @Positive Long testId,
                                                   @NotNull @Positive Long testCaseId,
                                                   @NotNull Pageable pageable) {

        checkIfTestExists(testId);

        TestCase parentTestCase = checkIfTestCaseExists(testCaseId);

        return testCaseMemberRepository.findAllByParentTestCase(parentTestCase, pageable);
    }

    public TestCaseMember createTestCaseMember(@NotNull TestCaseMember newTestCaseMember,
                                   @NotNull @Positive Long testId, @NotNull @Positive Long testCaseId) {

        Test parentTest = checkIfTestExists(testId);

        TestCase parentTestCase = checkIfTestCaseExists(testCaseId);

        if (StringUtils.isBlank(parentTest.getPath()) &&
                StringUtils.isNotBlank(newTestCaseMember.getRequestPathVariableValue())) {

            throw new InvalidTestCaseMemberConfigurationException("A test case member cannot set a path variable " +
                    " if the parent Test does not expose a path");
        }

        newTestCaseMember.setParentTestCase(parentTestCase);
        return testCaseMemberRepository.save(newTestCaseMember);
    }

    public void updateTestCaseMember(@NotNull @Positive Long testId, @NotNull @Positive Long testCaseId,
                                     @NotNull @Positive Long testCaseMemberId,
                                     @NotNull TestCaseMemberModel model) {

        Test parentTest = checkIfTestExists(testId);

        checkIfTestCaseExists(testCaseId);

        TestCaseMember existingTestCaseMember = checkIfTestCaseMemberExists(testCaseMemberId);

        if (model.getExpectedResponseCode() != null && model.getExpectedResponseCode() >= 0) {
            existingTestCaseMember.setExpectedResponseCode(model.getExpectedResponseCode());
        }

        setIfNotBlank(existingTestCaseMember::setExpectedResponseBody, model.getExpectedResponseBody());
        setIfNotBlank(existingTestCaseMember::setRequestPathVariableValue, model.getRequestPathVariableValue());
        setIfNotBlank(existingTestCaseMember::setRequestBody, model.getRequestBody());

        if (StringUtils.isBlank(parentTest.getPath()) &&
                StringUtils.isNotBlank(model.getRequestPathVariableValue())) {

            throw new InvalidTestCaseMemberConfigurationException("A test case member cannot set a path variable " +
                    " if the parent Test does not expose a path");
        }
        testCaseMemberRepository.save(existingTestCaseMember);
    }

    private void setIfNotBlank(Consumer<String> setter, String value) {

        if (StringUtils.isNotBlank(value)) {
            setter.accept(value);
        }
    }

    public void deleteTestCaseMember(@NotNull @Positive Long testId,
                                     @NotNull @Positive Long testCaseId,
                                     @NotNull @Positive Long testCaseMemberId) {

        checkIfTestExists(testId);

        checkIfTestCaseExists(testCaseId);

        TestCaseMember existingTestCaseMember = checkIfTestCaseMemberExists(testCaseMemberId);

        testCaseMemberRepository.delete(existingTestCaseMember);
    }


    private Test checkIfTestExists(Long parentTestId) {

        return testRepository.findById(parentTestId)
                .orElseThrow(() -> new TestNotFoundException(parentTestId));
    }

    private TestCase checkIfTestCaseExists(Long parentTestCaseId) {

        return testCaseRepository.findById(parentTestCaseId)
                .orElseThrow( () -> new TestCaseNotFoundException(parentTestCaseId));
    }

    private TestCaseMember checkIfTestCaseMemberExists(Long testCaseMemberId) {

        return testCaseMemberRepository.findById(testCaseMemberId)
                .orElseThrow( () -> new TestCaseMemberNotFoundException(testCaseMemberId));
    }


}
