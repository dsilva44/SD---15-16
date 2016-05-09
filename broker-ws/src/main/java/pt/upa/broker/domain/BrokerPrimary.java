package pt.upa.broker.domain;

import pt.upa.broker.ws.EndpointManager;

public class BrokerPrimary extends Broker {

    public BrokerPrimary(String uddiURL, EndpointManager epm) {
        super(uddiURL, epm);
    }

    @Override
    public void updateTransport(String tSerialized) {

    }

    @Override
    public void goNext() {
        BrokerBackup brokerBackup = new BrokerBackup(getUddiURL(), getEndPointManager());
        brokerBackup.registerUddi();
        Manager.getInstance().setCurrBroker(brokerBackup);
    }

    @Override
    public void monitor(long delay, long period) {

    }


}
