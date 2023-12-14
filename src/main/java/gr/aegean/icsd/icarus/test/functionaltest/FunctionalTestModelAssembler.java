package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCase;
import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCaseModel;
import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCaseModelAssembler;
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



    public FunctionalTestModelAssembler(TestCaseModelAssembler testCaseModelAssembler) {

        super(FunctionalTestController.class, FunctionalTestModel.class);
        this.testCaseModelAssembler = testCaseModelAssembler;
    }



    @Override
    @NonNull
    public FunctionalTestModel toModel(FunctionalTest entity) {

        FunctionalTestModel newModel = new FunctionalTestModel();

        newModel.setId(entity.getId());
        newModel.setName(entity.getName());
        newModel.setDescription(entity.getDescription());
        newModel.setHttpMethod(entity.getHttpMethod());

        newModel.setProviderPlatform(entity.getProviderPlatform());

        newModel.setTestAuthor(entity.getTestAuthor().getId());
        newModel.setTargetFunction(entity.getTargetFunction().getId());

        newModel.setRegion(entity.getRegion());
        newModel.setUsedMemory(entity.getUsedMemory());
        newModel.setFunctionUrl(entity.getFunctionURL());

        Set<TestCaseModel> testCases = new HashSet<>();
        for (TestCase testCase : entity.getTestCases()) {
            testCases.add(testCaseModelAssembler.toModel(testCase));
        }

        newModel.setTestCases(testCases);

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
