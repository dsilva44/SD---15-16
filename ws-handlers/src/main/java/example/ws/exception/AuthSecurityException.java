package example.ws.exception;


/**
 * Exception thrown when there is an security error condition.
 */
public class AuthSecurityException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuthSecurityException() {
    }

    public AuthSecurityException(String message) {
        super(message);
    }

    public AuthSecurityException(Throwable cause) {
        super(cause);
    }

    public AuthSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

}
