package pt.upa.broker.domain;

public class BrokerPrimary extends Broker {

    public BrokerPrimary(String primaryURL) {
        super(primaryURL);
    }

    @Override
    public void goNext() {
        Manager manager = Manager.getInstance();

        BrokerBackup brokerBackup = new BrokerBackup(getPrimaryURL());
        manager.getEndPointManager().registerUddi();
        manager.setCurrBroker(brokerBackup);
    }

    @Override
    public void monitor(long delay, long period) {

    }
}
