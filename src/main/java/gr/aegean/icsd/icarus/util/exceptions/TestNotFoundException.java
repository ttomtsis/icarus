package gr.aegean.icsd.icarus.util.exceptions;

public class TestNotFoundException extends RuntimeException {


    public TestNotFoundException(Long testId) {
        super("Test with id: " + testId + " was not found");
    }

}
