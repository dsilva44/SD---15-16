package pt.upa.ca.exception;


public abstract class CAException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CAException() {
    }

    public CAException(String msg) {
        super(msg);
    }
}

