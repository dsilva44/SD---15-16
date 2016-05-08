package pt.upa.broker.domain;

import pt.upa.broker.ws.EndpointManager;

public class BrokerBackup extends Broker {

    public BrokerBackup(String uddiURL, EndpointManager epm) {
        super(uddiURL, epm);
    }
}
