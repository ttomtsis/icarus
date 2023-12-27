package gr.aegean.icsd.icarus.util.exceptions.resourceconfiguration;

public class ResourceConfigurationNotFoundException extends RuntimeException {

    public ResourceConfigurationNotFoundException(Long configurationId) {
        super("Resource configuration with ID: " + configurationId + " does not exist");
    }


}
