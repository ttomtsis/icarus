package gr.aegean.icsd.icarus.test.functionaltest.testcasemember;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.defaultPageSize;


@RestController
@RequestMapping(value = "api/v0/tests/functional/{testId}/test-cases/{testCaseId}/test-case-members",
        produces = "application/json")
public class TestCaseMemberController {


    private final TestCaseMemberService service;
    private final TestCaseMemberModelAssembler modelAssembler;
    

    public TestCaseMemberController(TestCaseMemberService service,
                              TestCaseMemberModelAssembler assembler) {
        
        this.service = service;
        this.modelAssembler = assembler;
    }



    @GetMapping
    public ResponseEntity<PagedModel<TestCaseMemberModel>> getAllTestCaseMembers(@PathVariable Long testId,
                                                                     @PathVariable Long testCaseId,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = defaultPageSize) int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<TestCaseMember> testCaseMembers = service.getTestCaseMembers(testId, testCaseId, pageable);

        PagedModel<TestCaseMemberModel> testCaseMemberModels = modelAssembler
                .createPagedModel(testCaseMembers, testId, testCaseId);

        return ResponseEntity.ok().body(testCaseMemberModels);
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<TestCaseMemberModel> createTestCaseMember(@PathVariable Long testId,
                                                                    @PathVariable Long testCaseId,
                                                        @RequestBody TestCaseMemberModel TestCaseMemberModel) {

        TestCaseMember newTestCaseMember = TestCaseMember
                .createTestCaseMemberFromModel(TestCaseMemberModel);

        TestCaseMember savedTestCaseMember = service.createTestCaseMember(newTestCaseMember, testId, testCaseId);
        TestCaseMemberModel savedTestCaseMemberModel = modelAssembler.toModel(savedTestCaseMember, testId);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/v0/tests/functional/" + testId + "/test-cases/" + testCaseId +
                        "/test-case-members/" + savedTestCaseMember.getId())
                .buildAndExpand()
                .toUri();

        return ResponseEntity.created(location).body(savedTestCaseMemberModel);
    }

    @PutMapping(value = "/{testCaseMemberId}", consumes = "application/json")
    public ResponseEntity<Void> updateTestCaseMember(@PathVariable Long testId, @PathVariable Long testCaseId,
                                                     @PathVariable Long testCaseMemberId,
                                               @RequestBody TestCaseMemberModel model) {

        service.updateTestCaseMember(testId, testCaseId, testCaseMemberId, model);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{testCaseMemberId}")
    public ResponseEntity<Void> deleteTestCaseMember(@PathVariable Long testId, @PathVariable Long testCaseId,
                                                     @PathVariable Long testCaseMemberId) {

        service.deleteTestCaseMember(testId, testCaseId, testCaseMemberId);

        return ResponseEntity.noContent().build();
    }


}
