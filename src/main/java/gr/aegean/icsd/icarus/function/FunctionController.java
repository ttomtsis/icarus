package gr.aegean.icsd.icarus.function;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;


@RestController
@RequestMapping(value = "api/v0/tests/{testId}/functions", produces = "application/json")
public class FunctionController {


    private final FunctionService service;
    private final FunctionModelAssembler modelAssembler;

    
    
    public FunctionController(FunctionService service, FunctionModelAssembler assembler) {
        this.service = service;
        this.modelAssembler = assembler;
    }



    @GetMapping("/{functionId}")
    public ResponseEntity<FunctionModel> getFunction(@PathVariable Long testId,
                                                     @PathVariable Long functionId) {

        Function requestedFunction = service.getFunction(testId, functionId);
        FunctionModel requestedFunctionModel = modelAssembler.toModel(requestedFunction, testId);

        return ResponseEntity.ok().body(requestedFunctionModel);
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<FunctionModel> createFunction(@PathVariable Long testId,
                                                              @RequestBody FunctionModel functionModel) {

        Function newFunction = Function.createFunctionFromModel(functionModel);

        Function savedFunction = service.createFunction(newFunction, testId);
        FunctionModel savedFunctionModel = modelAssembler.toModel(savedFunction, testId);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/v0/tests/" + testId + "/functions/" + savedFunction.getId())
                .buildAndExpand()
                .toUri();

        return ResponseEntity.created(location).body(savedFunctionModel);
    }

    @PutMapping(value = "/{functionId}", consumes = "application/json")
    public ResponseEntity<Void> updateFunction(@PathVariable Long testId, @PathVariable Long functionId,
                                                  @RequestBody FunctionModel model) {

        service.updateFunction(testId, functionId, model);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{functionId}")
    public ResponseEntity<Void> deleteFunction(@PathVariable Long testId, @PathVariable Long functionId) {

        service.deleteFunction(testId, functionId);

        return ResponseEntity.noContent().build();
    }


}
