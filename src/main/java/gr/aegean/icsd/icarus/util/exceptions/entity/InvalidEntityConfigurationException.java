package gr.aegean.icsd.icarus.util.exceptions.entity;

public class InvalidEntityConfigurationException extends RuntimeException {


    public <T> InvalidEntityConfigurationException(Class<T> entityClass, Long id, String message) {
        super("Entity: " + entityClass.getSimpleName() + " with ID: " + id + " was not configured properly:\n"
                + message);
    }


    public <T> InvalidEntityConfigurationException(Class<T> entityClass, String message) {
        super("Entity: " + entityClass.getSimpleName() + " was not configured properly:\n"
                + message);
    }


    public <T> InvalidEntityConfigurationException(Class<T> entityClass, String message, Throwable throwable) {
        super("Entity: " + entityClass.getSimpleName() + " was not configured properly:\n"
                + message, throwable);
    }


}
