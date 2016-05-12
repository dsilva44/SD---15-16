package Exception;


/**
 * Exception thrown when there is an security error condition.
 */
public class SecurityException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SecurityException() {
    }

    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(Throwable cause) {
        super(cause);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }

}
