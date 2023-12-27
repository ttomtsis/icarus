package gr.aegean.icsd.icarus.util.exceptions.test;


public class InvalidTestConfigurationException extends RuntimeException {


    public InvalidTestConfigurationException(Long testId, String message) {
        super("Test with ID: " + testId + " " + message);
    }


    public InvalidTestConfigurationException(String message) {
        super(message);
    }


}
