package gr.aegean.icsd.icarus.test.performancetest;

import gr.aegean.icsd.icarus.test.performancetest.loadprofile.LoadProfile;
import gr.aegean.icsd.icarus.test.performancetest.loadprofile.LoadProfileModel;
import gr.aegean.icsd.icarus.test.performancetest.loadprofile.LoadProfileModelAssembler;
import gr.aegean.icsd.icarus.test.performancetest.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.test.performancetest.resourceconfiguration.ResourceConfigurationModel;
import gr.aegean.icsd.icarus.test.performancetest.resourceconfiguration.ResourceConfigurationModelAssembler;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class PerformanceTestModelAssembler
        extends RepresentationModelAssemblerSupport<PerformanceTest, PerformanceTestModel> {


    private final LoadProfileModelAssembler loadProfileModelAssembler;
    private final ResourceConfigurationModelAssembler resourceConfigurationModelAssembler;



    public PerformanceTestModelAssembler(LoadProfileModelAssembler loadProfileModelAssembler,
                                         ResourceConfigurationModelAssembler resourceConfigurationModelAssembler) {

        super(PerformanceTestController.class, PerformanceTestModel.class);
        this.loadProfileModelAssembler = loadProfileModelAssembler;
        this.resourceConfigurationModelAssembler = resourceConfigurationModelAssembler;

    }


    @NonNull
    @Override
    public PerformanceTestModel toModel(PerformanceTest entity) {

        PerformanceTestModel newModel = new PerformanceTestModel();

        newModel.setId(entity.getId());
        newModel.setName(entity.getName());
        newModel.setDescription(entity.getDescription());
        newModel.setHttpMethod(entity.getHttpMethod());

        // TODO: Create new Model for the author and function ?
        newModel.setTestAuthor(entity.getTestAuthor().getId());
        newModel.setTargetFunction(entity.getTargetFunction().getId());

        newModel.setPath(entity.getPath());
        newModel.setPathVariable(entity.getPathVariable());
        newModel.setPathVariableValue(entity.getPathVariableValue());

        newModel.setChosenMetrics(entity.getChosenMetrics());
        newModel.setRequestBody(entity.getRequestBody());

        Set<LoadProfileModel> loadProfiles = new HashSet<>();
        for (LoadProfile profile : entity.getLoadProfiles()) {
            loadProfiles.add(loadProfileModelAssembler.toModel(profile));
        }

        Set<ResourceConfigurationModel> resourceConfigurations = new HashSet<>();
        for (ResourceConfiguration configuration : entity.getResourceConfigurations()) {
            resourceConfigurations.add(resourceConfigurationModelAssembler.toModel(configuration));
        }

        newModel.setLoadProfiles(loadProfiles);
        newModel.setResourceConfigurations(resourceConfigurations);

        return addLinksToModel(newModel);
    }

    private PerformanceTestModel addLinksToModel(PerformanceTestModel model) {

        model.add(linkTo(methodOn(PerformanceTestController.class)
                .searchTest(model.getId())).withSelfRel());

        model.add(linkTo(methodOn(PerformanceTestController.class)
                .deleteTest(model.getId())).withRel("Delete"));

        model.add(linkTo(methodOn(PerformanceTestController.class)
                .updateTest(model.getId(), new PerformanceTestModel())).withRel("Update"));

        return model;
    }


}
