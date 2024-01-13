package gr.aegean.icsd.icarus.util.exceptions.entity;

public class ReportGenerationException extends RuntimeException{


    public ReportGenerationException(String deploymentId, Throwable cause) {
        super("Test execution with deployment ID: " + deploymentId + " failed to generate a report",
                cause);
    }


    public ReportGenerationException(String message) {
        super(message);
    }


    public ReportGenerationException(Throwable cause) {
        super(cause);
    }


}
