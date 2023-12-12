package gr.aegean.icsd.icarus.test.functionaltest.testcasemember;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;


@Component
public class TestCaseMemberModelAssembler
        extends RepresentationModelAssemblerSupport<TestCaseMember, TestCaseMemberModel> {

    public TestCaseMemberModelAssembler() {
        super(TestCaseMemberController.class, TestCaseMemberModel.class);
    }

    @Override
    public TestCaseMemberModel toModel(TestCaseMember entity) {
        return null;
    }

}
