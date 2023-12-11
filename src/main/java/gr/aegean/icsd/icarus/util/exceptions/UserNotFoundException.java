package gr.aegean.icsd.icarus.util.exceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String username) {
        super("User: " + username + ", not found");
    }

}
