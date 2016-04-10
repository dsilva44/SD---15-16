package pt.upa.transporter.exception;

public class CannotCreateUddiNamingException extends TransporterException {
    private static final long serialVersionUID = 1L;

    public CannotCreateUddiNamingException() {
        super("Error: cannot create instance of uddiNaming when");
    }

}
