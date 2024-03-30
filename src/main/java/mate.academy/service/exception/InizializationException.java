package mate.academy.service.exception;

public class InizializationException extends RuntimeException {
    public InizializationException(String message, Throwable exception) {
        super(message, exception);
    }
}
