package pt.upa.transporter.exception;

public class JobDoesNotExistException extends TransporterException {

    private static final long serialVersionUID = 1L;

    private String id;

    public JobDoesNotExistException(String name) {
        this.id = name;
    }

    private String getID() {return id;}

    @Override
    public String getMessage() {
        return getID() + " job not exists";
    }

}
