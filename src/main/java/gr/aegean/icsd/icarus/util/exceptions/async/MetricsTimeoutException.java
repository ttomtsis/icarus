package gr.aegean.icsd.icarus.util.exceptions.async;

public class MetricsTimeoutException extends RuntimeException{


    public MetricsTimeoutException(int minutes) {
        super("Timeout error: Metrics could not be queried, stopping after " + minutes + " minutes");
    }


}
