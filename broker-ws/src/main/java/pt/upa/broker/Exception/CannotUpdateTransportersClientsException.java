package pt.upa.broker.Exception;

public class CannotUpdateTransportersClientsException extends BrokerException {
    private static final long serialVersionUID = 1L;

    public CannotUpdateTransportersClientsException() {
        super("Cannot update transporters endpoints");
    }
}


