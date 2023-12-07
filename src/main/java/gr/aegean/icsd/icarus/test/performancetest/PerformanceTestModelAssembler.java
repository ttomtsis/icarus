package gr.aegean.icsd.icarus.test.performancetest;

import gr.aegean.icsd.icarus.test.performancetest.loadprofile.LoadProfile;
import gr.aegean.icsd.icarus.test.performancetest.loadprofile.LoadProfileModel;
import gr.aegean.icsd.icarus.test.performancetest.loadprofile.LoadProfileModelAssembler;
import gr.aegean.icsd.icarus.test.performancetest.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.test.performancetest.resourceconfiguration.ResourceConfigurationModel;
import gr.aegean.icsd.icarus.test.performancetest.resourceconfiguration.ResourceConfigurationModelAssembler;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;


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

        return newModel;
    }


}
