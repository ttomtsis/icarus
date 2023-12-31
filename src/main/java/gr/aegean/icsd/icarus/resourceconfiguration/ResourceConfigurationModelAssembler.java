package gr.aegean.icsd.icarus.resourceconfiguration;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.beans.support.PagedListHolder.DEFAULT_PAGE_SIZE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class ResourceConfigurationModelAssembler
        extends RepresentationModelAssemblerSupport<ResourceConfiguration, ResourceConfigurationModel> {


    public ResourceConfigurationModelAssembler() {
        super(ResourceConfigurationController.class, ResourceConfigurationModel.class);
    }



    @NonNull
    @Override
    public ResourceConfigurationModel toModel(ResourceConfiguration entity) {

        ResourceConfigurationModel newModel = new ResourceConfigurationModel(
                entity.getId(), entity.getProviderPlatform(), entity.getFunctionRuntime(),
                entity.getRegions(), entity.getCpuConfigurations(), entity.getMemoryConfigurations(),
                entity.getParentTest().getId()
        );

        newModel.setCreator(entity.getCreator().getUsername());

        return addLinks(newModel);
    }

    public PagedModel<ResourceConfigurationModel> createPagedModel(Page<ResourceConfiguration> configurations,
                                                                   Long testId) {

        PagedModel<ResourceConfigurationModel> pagedModel = createPagedModelFromPage(configurations);

        pagedModel.add(linkTo(methodOn(ResourceConfigurationController.class).getAllResourceConfigurations(testId,
                configurations.getNumber(), configurations.getSize())).withSelfRel());

        if (configurations.hasNext()) {
            pagedModel.add(linkTo(methodOn(ResourceConfigurationController.class).getAllResourceConfigurations(testId,
                    configurations.getNumber() + 1, configurations.getSize())).withRel("next"));
        }

        if (configurations.hasPrevious()) {
            pagedModel.add(linkTo(methodOn(ResourceConfigurationController.class).getAllResourceConfigurations(testId,
                    configurations.getNumber() - 1, configurations.getSize())).withRel("previous"));
        }

        return pagedModel;
    }

    private PagedModel<ResourceConfigurationModel> createPagedModelFromPage(Page<ResourceConfiguration> configurationPage) {

        List<ResourceConfigurationModel> configurationModels = configurationPage.getContent().stream()
                .map(this::toModel).toList();

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata
                (configurationPage.getSize(), configurationPage.getNumber(), configurationPage.getTotalElements());

        return PagedModel.of(configurationModels, pageMetadata);
    }

    public ResourceConfigurationModel addLinks(ResourceConfigurationModel model) {

        model.add(linkTo(methodOn(ResourceConfigurationController.class)
                .deleteConfiguration(model.getParentTest(), model.getId())).withRel("Delete"));

        model.add(linkTo(methodOn(ResourceConfigurationController.class)
                .updateConfiguration(model.getParentTest(), model.getId(),
                        new ResourceConfigurationModel())).withRel("Update"));

        model.add(linkTo(methodOn(ResourceConfigurationController.class)
                .getAllResourceConfigurations(model.getParentTest(), 0, DEFAULT_PAGE_SIZE))
                .withRel("Get all Resource configurations for this test"));

        return model;
    }

}
