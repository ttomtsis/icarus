package gr.aegean.icsd.icarus.util.exceptions.provideraccount;

public class ProviderAccountNotFoundException extends RuntimeException {

    public ProviderAccountNotFoundException(String accountName) {
        super("Account: " + accountName + ", was not found");
    }

}
