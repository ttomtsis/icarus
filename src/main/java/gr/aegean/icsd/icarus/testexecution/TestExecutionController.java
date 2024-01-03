package gr.aegean.icsd.icarus.testexecution;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.DEFAULT_PAGE_SIZE;


@RestController
@RequestMapping(value = "api/v0/tests/{testId}/executions", produces = "application/json")
public class TestExecutionController {


    private final TestExecutionService testExecutionService;
    private final TestExecutionModelAssembler modelAssembler;



    public TestExecutionController(TestExecutionService testExecutionService,
                                   TestExecutionModelAssembler modelAssembler) {
        this.testExecutionService = testExecutionService;
        this.modelAssembler = modelAssembler;
    }



    @GetMapping
    public ResponseEntity<PagedModel<TestExecutionModel>> getAllExecutions(@PathVariable Long testId,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<TestExecution> executions = testExecutionService.getExecutions(testId, pageable);
        PagedModel<TestExecutionModel> executionModels = modelAssembler
                .createPagedModel(executions, testId);

        return ResponseEntity.ok().body(executionModels);
    }


    @GetMapping("/{executionId}")
    public ResponseEntity<TestExecutionModel> getExecution(@PathVariable Long testId, @PathVariable Long executionId) {

        TestExecution execution = testExecutionService.getExecution(testId, executionId);
        TestExecutionModel executionModel = modelAssembler
                .toModel(execution);

        return ResponseEntity.ok().body(executionModel);
    }

    @GetMapping("/{deploymentId}/status")
    public ResponseEntity<String> getExecutionStatus(@PathVariable Long testId, @PathVariable String deploymentId) {

        String status = testExecutionService.getExecutionState(testId, deploymentId);

        return ResponseEntity.ok().body(status);
    }


    @DeleteMapping("/{executionId}")
    public ResponseEntity<Void> deleteExecution(@PathVariable Long testId, @PathVariable Long executionId) {

        testExecutionService.deleteExecution(testId, executionId);

        return ResponseEntity.noContent().build();
    }


}
