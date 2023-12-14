package gr.aegean.icsd.icarus.test.functionaltest.testcase;

import gr.aegean.icsd.icarus.test.functionaltest.testcasemember.TestCaseMember;
import gr.aegean.icsd.icarus.test.functionaltest.testcasemember.TestCaseMemberModel;
import gr.aegean.icsd.icarus.test.functionaltest.testcasemember.TestCaseMemberModelAssembler;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class TestCaseModelAssembler extends RepresentationModelAssemblerSupport<TestCase, TestCaseModel> {


    private final TestCaseMemberModelAssembler testCaseMemberModelAssembler;
    
    
    public TestCaseModelAssembler(TestCaseMemberModelAssembler testCaseMemberModelAssembler) {
        
        super(TestCaseController.class, TestCaseModel.class);
        this.testCaseMemberModelAssembler = testCaseMemberModelAssembler;
    }



    @Override
    @NonNull
    public TestCaseModel toModel(TestCase entity) {
        
        TestCaseModel newModel = new TestCaseModel();
        
        newModel.setId(entity.getId());
        newModel.setName(entity.getName());
        newModel.setDescription(entity.getDescription());
        newModel.setParentTest(entity.getParentTest().getId());

        Set<TestCaseMemberModel> members = new HashSet<>();
        for (TestCaseMember member : entity.getTestCaseMembers()) {
            members.add(testCaseMemberModelAssembler.toModel(member));
        }
        
        newModel.setTestCaseMembers(members);
        
        return addLinks(newModel);
    }

    public PagedModel<TestCaseModel> createPagedModel(Page<TestCase> testCases,
                                                                   Long testId) {

        PagedModel<TestCaseModel> pagedModel = createPagedModelFromPage(testCases);

        pagedModel.add(linkTo(methodOn(TestCaseController.class).getAllTestCases(testId,
                testCases.getNumber(), testCases.getSize())).withSelfRel());

        if (testCases.hasNext()) {
            pagedModel.add(linkTo(methodOn(TestCaseController.class).getAllTestCases(testId,
                    testCases.getNumber() + 1, testCases.getSize())).withRel("next"));
        }

        if (testCases.hasPrevious()) {
            pagedModel.add(linkTo(methodOn(TestCaseController.class).getAllTestCases(testId,
                    testCases.getNumber() - 1, testCases.getSize())).withRel("previous"));
        }

        return pagedModel;
    }

    private PagedModel<TestCaseModel> createPagedModelFromPage(Page<TestCase> testCasePage) {

        List<TestCaseModel> testCaseModels = testCasePage.getContent().stream()
                .map(this::toModel).toList();

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata
                (testCasePage.getSize(), testCasePage.getNumber(), testCasePage.getTotalElements());

        return PagedModel.of(testCaseModels, pageMetadata);
    }

    public TestCaseModel addLinks(TestCaseModel model) {

        model.add(linkTo(methodOn(TestCaseController.class)
                .deleteTestCase(model.getParentTest(), model.getId())).withRel("Delete"));

        model.add(linkTo(methodOn(TestCaseController.class)
                .updateTestCase(model.getParentTest(), model.getId(),
                        new TestCaseModel())).withRel("Update"));

        model.add(linkTo(methodOn(TestCaseController.class)
                .getAllTestCases(model.getParentTest(), 0, 10))
                .withRel("Get all Test cases for this test"));

        return model;
    }


}
