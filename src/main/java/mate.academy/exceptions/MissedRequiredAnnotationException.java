package mate.academy.exceptions;

public class MissedRequiredAnnotationException extends RuntimeException {
    public MissedRequiredAnnotationException(String message) {
        super(message);
    }
}
