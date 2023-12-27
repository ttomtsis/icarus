package gr.aegean.icsd.icarus.resourceconfiguration;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.DEFAULT_PAGE_SIZE;


@RestController
@RequestMapping(value = "api/v0/tests/{testId}/resource-configurations", produces = "application/json")
public class ResourceConfigurationController {


    private final ResourceConfigurationService service;
    private final ResourceConfigurationModelAssembler modelAssembler;



    public ResourceConfigurationController(ResourceConfigurationService service,
                                           ResourceConfigurationModelAssembler assembler) {
        this.service = service;
        this.modelAssembler = assembler;
    }



    @GetMapping
    public ResponseEntity<PagedModel<ResourceConfigurationModel>> getAllResourceConfigurations(@PathVariable Long testId,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ResourceConfiguration> configurations = service.getResourceConfigurations(testId, pageable);
        PagedModel<ResourceConfigurationModel> configurationModels = modelAssembler
                .createPagedModel(configurations, testId);

        return ResponseEntity.ok().body(configurationModels);
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<ResourceConfigurationModel> createConfiguration(@PathVariable Long testId,
                                                              @RequestBody ResourceConfigurationModel configurationModel) {

        ResourceConfiguration newConfiguration = ResourceConfiguration
                .createResourceConfigurationFromModel(configurationModel);

        ResourceConfiguration savedConfiguration = service.createConfiguration(newConfiguration, testId);
        ResourceConfigurationModel savedConfigurationModel = modelAssembler.toModel(savedConfiguration);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/v0/tests/" + testId + "/resource-configurations/" + savedConfiguration.getId())
                .buildAndExpand()
                .toUri();

        return ResponseEntity.created(location).body(savedConfigurationModel);
    }

    @PutMapping(value = "/{configurationId}", consumes = "application/json")
    public ResponseEntity<Void> updateConfiguration(@PathVariable Long testId, @PathVariable Long configurationId,
                                                  @RequestBody ResourceConfigurationModel model) {

        service.updateConfiguration(testId, configurationId, model);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{configurationId}")
    public ResponseEntity<Void> deleteConfiguration(@PathVariable Long testId, @PathVariable Long configurationId) {

        service.deleteConfiguration(testId, configurationId);

        return ResponseEntity.noContent().build();
    }


}
