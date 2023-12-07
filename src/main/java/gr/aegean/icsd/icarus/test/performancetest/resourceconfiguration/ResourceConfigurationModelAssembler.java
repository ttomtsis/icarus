package gr.aegean.icsd.icarus.test.performancetest.resourceconfiguration;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;


@Component
public class ResourceConfigurationModelAssembler
        extends RepresentationModelAssemblerSupport<ResourceConfiguration, ResourceConfigurationModel> {
    public ResourceConfigurationModelAssembler() {
        super(ResourceConfigurationController.class, ResourceConfigurationModel.class);
    }

    @Override
    public ResourceConfigurationModel toModel(ResourceConfiguration entity) {
        return null;
    }
}
