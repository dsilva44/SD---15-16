package pt.upa.transporter.exception;

public class InvalidURLException extends TransporterException {
    private static final long serialVersionUID = 1L;

    private String url;

    public InvalidURLException(String url) {
        this.url = url;
    }

    public String getURL() {
        return url;
    }

    @Override
    public String getMessage() {
        return getURL() + " is not a valid URL";
    }
}
