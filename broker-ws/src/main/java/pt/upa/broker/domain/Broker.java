package pt.upa.broker.domain;

public abstract class Broker {
    public abstract void updateTransport(String tSerialized);
    public abstract void goNext();
    public abstract void monitor(long delay, long period);
}
