package gr.aegean.icsd.icarus.util.exceptions;

public class ProviderAccountNotFoundException extends RuntimeException {

    public ProviderAccountNotFoundException(String accountName) {
        super("Account: " + accountName + ", was not found");
    }

}
