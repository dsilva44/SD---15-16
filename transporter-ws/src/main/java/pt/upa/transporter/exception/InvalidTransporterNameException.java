package pt.upa.transporter.exception;

public class InvalidTransporterNameException extends TransporterException {
    private static final long serialVersionUID = 1L;

    private String wsName;

    public InvalidTransporterNameException(String wsName) {
        this.wsName = wsName;
    }

    public String getWsName() {
        return wsName;
    }

    @Override
    public String getMessage() {
        return getWsName() + " is not a valid Transporter Name";
    }
}

