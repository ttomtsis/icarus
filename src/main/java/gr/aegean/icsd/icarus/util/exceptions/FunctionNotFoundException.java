package gr.aegean.icsd.icarus.util.exceptions;

public class FunctionNotFoundException extends RuntimeException {

    public FunctionNotFoundException(Long functionId) {
        super("Load Profile with ID: " + functionId + " does not exist");
    }


}
