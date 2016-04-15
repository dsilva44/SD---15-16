package pt.upa.broker.domain;

import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.cli.TransporterClient;

import java.util.ArrayList;
import java.util.List;

public class TransportOffers {
    private Transport transport;
    private List<JobView> offers;
    private int referencePrice;
    private TransporterClient transporterClient = null;

    public TransportOffers(Transport transport, int referencePrice) {
        this.transport = transport;
        this.referencePrice = referencePrice;
        offers = new ArrayList<>();
    }

    public List<JobView> getOffers() {
        return offers;
    }

    public void addOffer(JobView offer) {
        offers.add(offer);
    }

    public Transport getTransport() {
        return transport;
    }

    public TransporterClient getTransporterClient() { return transporterClient; }

    public void setTransporterClient(TransporterClient transporterClient) {
        this.transporterClient = transporterClient;
    }

    public int getReferencePrice() {
        return referencePrice;
    }
}
