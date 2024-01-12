package gr.aegean.icsd.icarus.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.SerializationException;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;


@RestController
@RequestMapping(value = "api/v0/functions", produces = "application/json")
public class FunctionController {


    private final FunctionService service;
    private final FunctionModelAssembler modelAssembler;

    
    
    public FunctionController(FunctionService service, FunctionModelAssembler assembler) {
        this.service = service;
        this.modelAssembler = assembler;
    }



    @GetMapping("/{functionId}")
    public ResponseEntity<FunctionModel> getFunction(@PathVariable Long functionId) {

        Function requestedFunction = service.getFunction(functionId);
        FunctionModel requestedFunctionModel = modelAssembler.toModel(requestedFunction);

        return ResponseEntity.ok().body(requestedFunctionModel);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<FunctionModel> createFunction(@RequestPart("functionMetadata") String textModel,
                                                        @RequestPart("functionSource") MultipartFile functionSource) {

        FunctionModel functionModel = serializeToModel(textModel);

        Function newFunction = Function.createFunctionFromModel(functionModel);

        Function savedFunction = null;
        try {
            savedFunction = service.createFunction(newFunction, functionSource);
        } catch (IOException e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        assert savedFunction != null;
        FunctionModel savedFunctionModel = modelAssembler.toModel(savedFunction);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/v0/functions/" + savedFunction.getId())
                .buildAndExpand()
                .toUri();

        return ResponseEntity.created(location).body(savedFunctionModel);
    }

    @PutMapping(value = "/{functionId}", consumes = "multipart/form-data")
    public ResponseEntity<Void> updateFunction(@PathVariable Long functionId,
                                               @RequestPart(name = "functionMetadata", required = false) String textModel,
                                               @RequestPart(name = "functionSource", required = false) MultipartFile functionSource) {

        if (StringUtils.isBlank(textModel) && functionSource == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        FunctionModel functionModel = null;
        if (textModel != null) {
            functionModel = serializeToModel(textModel);
        }

        try {
            service.updateFunction(functionId, functionModel, functionSource);
        }
        catch (IOException ex) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{functionId}")
    public ResponseEntity<Void> deleteFunction(@PathVariable Long functionId) {

        try {
            service.deleteFunction(functionId);
        }
        catch (IOException ex) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.noContent().build();
    }



    @NotNull
    private FunctionModel serializeToModel(String textModel) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(textModel, FunctionModel.class);
        }
        catch (IOException ex) {
            LoggerFactory.getLogger(FunctionController.class).error("Error when serializing {} to FunctionModel: \n" +
                    "{}", textModel, Arrays.toString(ex.getStackTrace()));
            throw new SerializationException("Serialization Failed due to IOException", ex);
        }
    }



}
