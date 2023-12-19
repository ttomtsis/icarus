package gr.aegean.icsd.icarus.util.exceptions.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestExecutionFailedException extends RuntimeException {


    private static final Logger log = LoggerFactory.getLogger("Test Execution Exception");


    public TestExecutionFailedException(Throwable throwable) {

        super();

        String errorMessage = String.format("Async exception caught: " +
                        "%nCause: %s" +
                        " %nMessage: %s",
                throwable.getCause().getClass().getSimpleName(), throwable.getCause().getMessage());

        log.error(errorMessage);
    }



}
