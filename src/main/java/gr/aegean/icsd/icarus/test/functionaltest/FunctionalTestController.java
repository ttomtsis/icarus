package gr.aegean.icsd.icarus.test.functionaltest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;


@RestController
@RequestMapping(value = "api/v0/tests/functional", produces = "application/json")
public class FunctionalTestController {


    private final FunctionalTestService service;
    private final FunctionalTestModelAssembler assembler;



    public FunctionalTestController(FunctionalTestService service, FunctionalTestModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }



    @GetMapping("/{testId}")
    public ResponseEntity<FunctionalTestModel> searchTest(@PathVariable Long testId) {

        FunctionalTest requestedTest = service.searchTest(testId);

        FunctionalTestModel requestedTestModel = assembler.toModel(requestedTest);

        return ResponseEntity.ok().body(requestedTestModel);
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<FunctionalTestModel> createTest(@RequestBody FunctionalTestModel testModel) {

        FunctionalTest newTest = FunctionalTest.createFunctionalTestFromModel(testModel);

        FunctionalTest savedTest = service.createTest(newTest);

        FunctionalTestModel savedTestModel = assembler.toModel(savedTest);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/v0/tests/functional/" + savedTest.getId())
                .buildAndExpand()
                .toUri();

        return ResponseEntity.created(location).body(savedTestModel);
    }

    @PutMapping(value = "/{testId}", consumes = "application/json")
    public ResponseEntity<Void> updateTest(@PathVariable Long testId, @RequestBody FunctionalTestModel testModel) {

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
