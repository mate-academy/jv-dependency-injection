package mate.academy.exceptions;

public class InvalidImplementationException extends RuntimeException {
    public InvalidImplementationException(Class<?> clazz) {
        super("Can't create implementation of class " + clazz.getName());
    }
}
