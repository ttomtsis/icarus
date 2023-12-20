package gr.aegean.icsd.icarus.util.exceptions.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestExecutionFailedException extends RuntimeException {


    private static final Logger log = LoggerFactory.getLogger("Test Execution Exception");


    public TestExecutionFailedException(Throwable throwable) {
        super();

        if (throwable.getCause() != null) {

            StringBuilder stackTrace = new StringBuilder();
            for (StackTraceElement element : throwable.getCause().getStackTrace()) {
                stackTrace.append("\n\tat ").append(element);
            }

            String errorMessage = String.format("Async exception caught: " +
                            "%nCause: %s" +
                            "%nMessage: %s" +
                            "%nStack Trace: %s",
                    throwable.getCause().getClass().getSimpleName(),
                    throwable.getCause().getMessage(),
                    stackTrace
            );

            log.error(errorMessage);
        }
        else {

            StringBuilder stackTrace = new StringBuilder();
            for (StackTraceElement element : throwable.getStackTrace()) {
                stackTrace.append("\n\tat ").append(element);
            }

            String errorMessage = String.format("Async exception caught: " +
                            "%nCause: %s" +
                            "%nMessage: %s" +
                            "%nStack Trace: %s",
                    throwable.getClass().getSimpleName(),
                    throwable.getMessage(),
                    stackTrace
            );

            log.error(errorMessage);
        }

    }




}
