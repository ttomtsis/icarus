package gr.aegean.icsd.icarus.util.exceptions;

public class InvalidPasswordException extends RuntimeException {


    public InvalidPasswordException(String password) {
        super("The password: " + password + " does not conform to password limitations");
    }


}
