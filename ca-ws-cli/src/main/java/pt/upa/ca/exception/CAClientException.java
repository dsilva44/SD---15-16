package pt.upa.ca.exception;

/**
 *	Exception thrown by the Client when there is an unexpected error condition.  
 */
public class CAClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CAClientException() {
    }

    public CAClientException(String message) {
        super(message);
    }

    public CAClientException(Throwable cause) {
        super(cause);
    }

    public CAClientException(String message, Throwable cause) {
        super(message, cause);
    }

}
