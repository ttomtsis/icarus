package gr.aegean.icsd.icarus.util.exceptions.entity;


public class EntityNotFoundException extends RuntimeException {


    public <T> EntityNotFoundException(Class<T> entityClass, Long id) {
        super(entityClass.getSimpleName() + " with ID: " + id + " was not found");
    }

    public <T> EntityNotFoundException(Class<T> entityClass, String name) {
        super(entityClass.getSimpleName() + " with name: " + name + " was not found");
    }

}
