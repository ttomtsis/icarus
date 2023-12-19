package gr.aegean.icsd.icarus.util.exceptions.testcasemember;

public class TestCaseMemberNotFoundException extends RuntimeException {

    public TestCaseMemberNotFoundException(Long testCaseMemberId) {
        super("Test case member with ID: " + testCaseMemberId + " does not exist");
    }


}
