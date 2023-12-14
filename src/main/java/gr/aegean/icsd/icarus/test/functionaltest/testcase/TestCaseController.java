package gr.aegean.icsd.icarus.test.functionaltest.testcase;

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
@RequestMapping(value = "api/v0/tests/functional/{testId}/test-cases" ,produces = "application/json")
public class TestCaseController {

    
    private final TestCaseService service;
    private final TestCaseModelAssembler modelAssembler;



    public TestCaseController(TestCaseService service,
                                           TestCaseModelAssembler assembler) {
        this.service = service;
        this.modelAssembler = assembler;
    }



    @GetMapping
    public ResponseEntity<PagedModel<TestCaseModel>> getAllTestCases(@PathVariable Long testId,
                                                                                               @RequestParam(defaultValue = "0") int page,
                                                                                               @RequestParam(defaultValue = defaultPageSize) int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<TestCase> TestCases = service.getTestCases(testId, pageable);
        PagedModel<TestCaseModel> TestCaseModels = modelAssembler
                .createPagedModel(TestCases, testId);

        return ResponseEntity.ok().body(TestCaseModels);
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<TestCaseModel> createTestCase(@PathVariable Long testId,
                                                                          @RequestBody TestCaseModel TestCaseModel) {

        TestCase newTestCase = TestCase
                .createTestCaseFromModel(TestCaseModel);

        TestCase savedTestCase = service.createTestCase(newTestCase, testId);
        TestCaseModel savedTestCaseModel = modelAssembler.toModel(savedTestCase);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/v0/tests/functional/" + testId + "/test-cases/" + savedTestCase.getId())
                .buildAndExpand()
                .toUri();

        return ResponseEntity.created(location).body(savedTestCaseModel);
    }

    @PutMapping(value = "/{TestCaseId}", consumes = "application/json")
    public ResponseEntity<Void> updateTestCase(@PathVariable Long testId, @PathVariable Long TestCaseId,
                                                    @RequestBody TestCaseModel model) {

        service.updateTestCase(testId, TestCaseId, model);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{TestCaseId}")
    public ResponseEntity<Void> deleteTestCase(@PathVariable Long testId, @PathVariable Long TestCaseId) {

        service.deleteTestCase(testId, TestCaseId);

        return ResponseEntity.noContent().build();
    }


}
