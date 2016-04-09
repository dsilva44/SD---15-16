package pt.upa.transporter.exception;

public abstract class TransporterException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TransporterException() {
    }

    public TransporterException(String msg) {
        super(msg);
    }
}
