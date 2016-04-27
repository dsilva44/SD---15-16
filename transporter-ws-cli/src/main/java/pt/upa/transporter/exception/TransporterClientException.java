package pt.upa.transporter.exception;

/**
 * Exception thrown by the Client when there is an unexpected error condition.
 */
public class TransporterClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TransporterClientException() {
	}

	public TransporterClientException(String message) {
		super(message);
	}

	public TransporterClientException(Throwable cause) {
		super(cause);
	}

	public TransporterClientException(String message, Throwable cause) {
		super(message, cause);
	}

}
