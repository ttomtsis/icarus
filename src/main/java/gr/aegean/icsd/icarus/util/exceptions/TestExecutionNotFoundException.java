package gr.aegean.icsd.icarus.util.exceptions;

public class TestExecutionNotFoundException extends RuntimeException{

    public TestExecutionNotFoundException(Long id) {
        super("Test execution with id: " + id + " was not found");
    }
}
