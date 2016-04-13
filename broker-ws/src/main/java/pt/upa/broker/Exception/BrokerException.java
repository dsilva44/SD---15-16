package pt.upa.broker.Exception;


public abstract class BrokerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BrokerException() {
    }

    public BrokerException(String msg) {
        super(msg);
    }
}

