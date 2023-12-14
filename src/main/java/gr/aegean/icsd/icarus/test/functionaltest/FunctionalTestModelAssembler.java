package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCase;
import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCaseModel;
import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCaseModelAssembler;
import gr.aegean.icsd.icarus.test.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.test.resourceconfiguration.ResourceConfigurationModel;
import gr.aegean.icsd.icarus.test.resourceconfiguration.ResourceConfigurationModelAssembler;
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


    private final TestCaseModelAssembler testCaseModelAssembler;
    private final ResourceConfigurationModelAssembler resourceConfigurationModelAssembler;



    public FunctionalTestModelAssembler(TestCaseModelAssembler testCaseModelAssembler,
                                        ResourceConfigurationModelAssembler resourceConfigurationModelAssembler) {

        super(FunctionalTestController.class, FunctionalTestModel.class);
        this.testCaseModelAssembler = testCaseModelAssembler;
        this.resourceConfigurationModelAssembler = resourceConfigurationModelAssembler;
    }



    @Override
    @NonNull
    public FunctionalTestModel toModel(FunctionalTest entity) {

        FunctionalTestModel newModel = new FunctionalTestModel();

        newModel.setId(entity.getId());
        newModel.setName(entity.getName());
        newModel.setDescription(entity.getDescription());
        newModel.setHttpMethod(entity.getHttpMethod());

        newModel.setTestAuthor(entity.getTestAuthor().getId());
        newModel.setTargetFunction(entity.getTargetFunction().getId());

        newModel.setPath(entity.getPath());
        newModel.setPathVariable(entity.getPathVariable());

        newModel.setRegion(entity.getRegion());
        newModel.setUsedMemory(entity.getUsedMemory());
        newModel.setFunctionUrl(entity.getFunctionURL());


        Set<TestCaseModel> testCases = new HashSet<>();
        for (TestCase testCase : entity.getTestCases()) {
            testCases.add(testCaseModelAssembler.toModel(testCase));
        }

        Set<ResourceConfigurationModel> resourceConfigurations = new HashSet<>();
        for (ResourceConfiguration configuration : entity.getResourceConfigurations()) {
            resourceConfigurations.add(resourceConfigurationModelAssembler.toModel(configuration));
        }


        newModel.setTestCases(testCases);
        newModel.setResourceConfigurations(resourceConfigurations);

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
