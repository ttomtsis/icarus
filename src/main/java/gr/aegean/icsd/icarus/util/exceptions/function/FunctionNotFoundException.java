package gr.aegean.icsd.icarus.util.exceptions.function;

public class FunctionNotFoundException extends RuntimeException {

    public FunctionNotFoundException(Long functionId) {
        super("Function with ID: " + functionId + " does not exist");
    }


}
