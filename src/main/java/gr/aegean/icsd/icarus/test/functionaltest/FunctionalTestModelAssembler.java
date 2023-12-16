package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.test.TestModel;
import gr.aegean.icsd.icarus.test.TestModelAssembler;
import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCase;
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
public class FunctionalTestModelAssembler
        extends RepresentationModelAssemblerSupport<FunctionalTest, FunctionalTestModel> {


    private final TestModelAssembler testModelAssembler;



    public FunctionalTestModelAssembler(TestModelAssembler testModelAssembler) {

        super(FunctionalTestController.class, FunctionalTestModel.class);
        this.testModelAssembler = testModelAssembler;
    }



    @Override
    @NonNull
    public FunctionalTestModel toModel(@NotNull FunctionalTest entity) {

        TestModel parentModel = testModelAssembler.toModel(entity);

        Set<Long> testCases = new HashSet<>();
        for (TestCase testCase : entity.getTestCases()) {
            testCases.add(testCase.getId());
        }

        Set<Long> resourceConfigurations = new HashSet<>();
        for (ResourceConfiguration configuration : entity.getResourceConfigurations()) {
            resourceConfigurations.add(configuration.getId());
        }


        FunctionalTestModel newModel = new FunctionalTestModel(parentModel, entity.getFunctionURL(),
                testCases, resourceConfigurations);

        return addLinksToModel(newModel);
    }

    private FunctionalTestModel addLinksToModel(FunctionalTestModel model) {

        model.add(linkTo(methodOn(FunctionalTestController.class)
                .searchTest(model.getId())).withSelfRel());

        model.add(linkTo(methodOn(FunctionalTestController.class)
                .deleteTest(model.getId())).withRel("Delete"));

        model.add(linkTo(methodOn(FunctionalTestController.class)
                .updateTest(model.getId(), new FunctionalTestModel())).withRel("Update"));

        return model;
    }


}
