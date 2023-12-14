package gr.aegean.icsd.icarus.util.exceptions;

public class LoadProfileNotFoundException extends RuntimeException{

    public LoadProfileNotFoundException(Long loadProfileId) {
        super("Load Profile with ID: " + loadProfileId + " does not exist");
    }


}
