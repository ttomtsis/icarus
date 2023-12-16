package gr.aegean.icsd.icarus.test.performancetest;

import gr.aegean.icsd.icarus.test.TestModel;
import gr.aegean.icsd.icarus.test.TestModelAssembler;
import gr.aegean.icsd.icarus.test.performancetest.loadprofile.LoadProfile;
import gr.aegean.icsd.icarus.test.resourceconfiguration.ResourceConfiguration;
import org.jetbrains.annotations.NotNull;
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


    private final TestModelAssembler testModelAssembler;



    public PerformanceTestModelAssembler(TestModelAssembler testModelAssembler) {

        super(PerformanceTestController.class, PerformanceTestModel.class);
        this.testModelAssembler = testModelAssembler;
    }


    @NonNull
    @Override
    public PerformanceTestModel toModel(@NotNull PerformanceTest entity) {

        TestModel parentModel = testModelAssembler.toModel(entity);

        Set<Long> loadProfiles = new HashSet<>();
        for (LoadProfile profile : entity.getLoadProfiles()) {
            loadProfiles.add(profile.getId());
        }

        Set<Long> resourceConfigurations = new HashSet<>();
        for (ResourceConfiguration configuration : entity.getResourceConfigurations()) {
            resourceConfigurations.add(configuration.getId());
        }

        PerformanceTestModel newModel = new PerformanceTestModel(parentModel, entity.getChosenMetrics(),
                entity.getPathVariableValue(), entity.getRequestBody(), loadProfiles, resourceConfigurations);

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
