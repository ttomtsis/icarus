package gr.aegean.icsd.icarus.util.exceptions;

public class TestCaseNotFoundException extends RuntimeException {

    public TestCaseNotFoundException(Long testCaseId) {
        super("Test Case with ID: " + testCaseId + " does not exist");
    }


}
