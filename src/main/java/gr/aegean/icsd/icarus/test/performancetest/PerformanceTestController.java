package gr.aegean.icsd.icarus.test.performancetest;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(value = "api/v0/tests/performance", produces = "application/json")
public class PerformanceTestController {


    private final PerformanceTestService service;
    private final PerformanceTestModelAssembler assembler;



    public PerformanceTestController(PerformanceTestService service, PerformanceTestModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
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




}
