package gr.aegean.icsd.icarus.test.performancetest;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(value = "api/v0/tests/performance", produces = "application/json")
public class PerformanceTestController {


    private final PerformanceTestService service;
    private final PerformanceTestModelAssembler assembler;



    public PerformanceTestController(PerformanceTestService service, PerformanceTestModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }



    @GetMapping("/{testId}")
    public ResponseEntity<PerformanceTestModel> searchTest(@PathVariable Long testId) {

        PerformanceTest requestedTest = service.searchTest(testId);

        PerformanceTestModel requestedTestModel = assembler.toModel(requestedTest);

        return ResponseEntity.ok().body(requestedTestModel);
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<PerformanceTestModel> createTest(@RequestBody PerformanceTestModel testModel) {

        PerformanceTest newTest = PerformanceTest.createPerformanceTestFromModel(testModel);

        PerformanceTest savedTest = service.createTest(newTest);

        PerformanceTestModel savedTestModel = assembler.toModel(savedTest);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/v0/tests/performance/" + savedTest.getId())
                .buildAndExpand()
                .toUri();

        return ResponseEntity.created(location).body(savedTestModel);
    }

    @PutMapping(value = "/{testId}", consumes = "application/json")
    public ResponseEntity<Void> updateTest(@PathVariable Long testId, @RequestBody PerformanceTestModel testModel) {

        service.updateTest(testId, testModel);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{testId}")
    public ResponseEntity<Void> deleteTest(@PathVariable Long testId) {

        service.deleteTest(testId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{testId}/execute")
    public ResponseEntity<Void> executeTest(@PathVariable Long testId) {

        String deploymentId = UUID.randomUUID().toString().substring(0, 5);
        service.executeTest(testId, deploymentId);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v0/tests/{testId}/executions/" + deploymentId + "/status")
                .buildAndExpand(testId)
                .toUri();

        return ResponseEntity.accepted().header("Location", location.toString()).build();
    }


}
