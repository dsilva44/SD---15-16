package pt.upa.broker.domain;

import pt.upa.broker.ws.EndpointManager;

public class BrokerPrimary extends Broker{

    public BrokerPrimary(String uddiURL, EndpointManager epm) {
        super(uddiURL, epm);
    }
}
