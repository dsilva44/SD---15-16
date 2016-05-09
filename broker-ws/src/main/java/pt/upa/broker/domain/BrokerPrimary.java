package pt.upa.broker.domain;

public class BrokerPrimary extends Broker {

    @Override
    public void updateTransport(String tSerialized) {

    }

    @Override
    public void goNext() {
        Manager manager = Manager.getInstance();

        BrokerBackup brokerBackup = new BrokerBackup();
        manager.getEndPointManager().registerUddi();
        manager.setCurrBroker(brokerBackup);
    }

    @Override
    public void monitor(long delay, long period) {

    }


}
