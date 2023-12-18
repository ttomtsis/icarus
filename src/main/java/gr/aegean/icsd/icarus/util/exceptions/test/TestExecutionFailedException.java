package gr.aegean.icsd.icarus.util.exceptions.test;

public class TestExecutionFailedException extends RuntimeException {

    public TestExecutionFailedException(Long testId, String testType, String message) {

        super(testType + " with id: " + testId + " failed to execute.\n" + message);
    }


}
