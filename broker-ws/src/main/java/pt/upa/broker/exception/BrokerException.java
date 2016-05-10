package pt.upa.broker.exception;


public class BrokerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BrokerException() {
    }

    public BrokerException(String message) {
        super(message);
    }

    public BrokerException(Throwable cause) {
        super(cause);
    }

    public BrokerException(String message, Throwable cause) {
        super(message, cause);
    }
}

