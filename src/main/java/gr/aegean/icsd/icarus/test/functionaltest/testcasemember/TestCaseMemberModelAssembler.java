package gr.aegean.icsd.icarus.test.functionaltest.testcasemember;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class TestCaseMemberModelAssembler
        extends RepresentationModelAssemblerSupport<TestCaseMember, TestCaseMemberModel> {


    public TestCaseMemberModelAssembler() {
        super(TestCaseMemberController.class, TestCaseMemberModel.class);
    }



    @Override
    public @NotNull TestCaseMemberModel toModel(@NotNull TestCaseMember entity) {
        throw new UnsupportedOperationException("toModel(entity) in TestCseMemberModelAssembler" +
                " should not have been invoked");
    }


    public TestCaseMemberModel toModel(TestCaseMember entity, Long testId) {

        TestCaseMemberModel newModel = new TestCaseMemberModel();

        newModel.setId(entity.getId());
        newModel.setParentTestCase(entity.getParentTestCase().getId());
        newModel.setExpectedResponseBody(entity.getExpectedResponseBody());
        newModel.setExpectedResponseCode(entity.getExpectedResponseCode());
        newModel.setRequestPathVariable(entity.getRequestPathVariable());

        return addLinks(newModel, testId);
    }

    public PagedModel<TestCaseMemberModel> createPagedModel(Page<TestCaseMember> testCaseMembers,
                                                      Long testId, Long testCaseId) {

        PagedModel<TestCaseMemberModel> pagedModel = createPagedModelFromPage(testCaseMembers, testId);

        pagedModel.add(linkTo(methodOn(TestCaseMemberController.class).getAllTestCaseMembers(testId, testCaseId,
                testCaseMembers.getNumber(), testCaseMembers.getSize())).withSelfRel());

        if (testCaseMembers.hasNext()) {
            pagedModel.add(linkTo(methodOn(TestCaseMemberController.class).getAllTestCaseMembers(testId, testCaseId,
                    testCaseMembers.getNumber() + 1, testCaseMembers.getSize())).withRel("next"));
        }

        if (testCaseMembers.hasPrevious()) {
            pagedModel.add(linkTo(methodOn(TestCaseMemberController.class).getAllTestCaseMembers(testId, testCaseId,
                    testCaseMembers.getNumber() - 1, testCaseMembers.getSize())).withRel("previous"));
        }

        return pagedModel;
    }

    private PagedModel<TestCaseMemberModel> createPagedModelFromPage(Page<TestCaseMember> testCaseMembersPage,
                                                                     Long testId) {

        List<TestCaseMemberModel> testCaseMemberModels = testCaseMembersPage.getContent().stream()
                .map(testCaseMember -> this.toModel(testCaseMember, testId)).toList();

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata
                (testCaseMembersPage.getSize(), testCaseMembersPage.getNumber(),
                        testCaseMembersPage.getTotalElements());

        return PagedModel.of(testCaseMemberModels, pageMetadata);
    }

    public TestCaseMemberModel addLinks(TestCaseMemberModel model, Long testId) {

        model.add(linkTo(methodOn(TestCaseMemberController.class)
                .deleteTestCaseMember(testId, model.getParentTestCase(), model.getId()))
                .withRel("Delete"));

        model.add(linkTo(methodOn(TestCaseMemberController.class)
                .updateTestCaseMember(testId, model.getParentTestCase(), model.getId(),
                        new TestCaseMemberModel())).withRel("Update"));

        model.add(linkTo(methodOn(TestCaseMemberController.class)
                .getAllTestCaseMembers(testId, model.getParentTestCase(), 0, 10))
                .withRel("Get all Test Case Members for this test case"));

        return model;
    }


}
