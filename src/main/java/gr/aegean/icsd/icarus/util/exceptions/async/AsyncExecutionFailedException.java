package gr.aegean.icsd.icarus.util.exceptions.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AsyncExecutionFailedException extends RuntimeException {


    private static final Logger log = LoggerFactory.getLogger("Test Execution Exception");



    public AsyncExecutionFailedException(Throwable throwable) {

        super();
        printDetails(throwable);
    }


    public AsyncExecutionFailedException(String message) {

        super(message);

        String errorMessage = String.format("Async exception caught: %s",  message);
        log.error(errorMessage);
    }


    public AsyncExecutionFailedException(String message, Throwable throwable) {

        super(message, throwable);
        printDetails(throwable);
    }



    private void printDetails(Throwable throwable) {

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
