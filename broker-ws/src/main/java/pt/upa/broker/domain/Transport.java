package pt.upa.broker.domain;

import pt.upa.broker.ws.TransportStateView;
import pt.upa.broker.ws.TransportView;
import pt.upa.transporter.ws.JobView;

import java.util.ArrayList;
import java.util.List;

public class Transport {
	
	private String id;
    private String origin;
    private String destination;
    private int price;
    private String transporterCompany;
    private TransportStateView state;

	private List<JobView> offers = new ArrayList<>();
	private String chosenOfferID = null;
	 
	public Transport () {
	
	}
	
	public Transport(String id, String origin, String destination, int price, String transporterCompany, TransportStateView state) {
	    this.id = id;
	    this.origin = origin;
	    this.destination = destination;
	    this.price = price;
	    this.transporterCompany = transporterCompany;
	    this.state = state;
	}
	
	
	public String getId() {
			return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getOrigin() {
		return origin;
	}
	
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	
	public String getDestination() {
		return destination;
	}
	
	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	public int getPrice() {
		return price;
	}
	
	public void setPrice(int price) {
		this.price = price;
	}
	
	public String getTransporterCompany() {
		return transporterCompany;
	}
	
	public void setTransporterCompany(String transporterCompany) {
		this.transporterCompany = transporterCompany;
	}
	
	public TransportStateView getState() {
		return state;
	}
	
	public void setState(TransportStateView state) {
		this.state = state;
	}

	public List<JobView> getOffers() {
		return offers;
	}

	public void addOffer(JobView offer) {
		offers.add(offer);
	}

	public String getChosenOfferID() {
		return chosenOfferID;
	}

	public void setChosenOfferID(String chosenOfferID) {
		this.chosenOfferID = chosenOfferID;
	}
	
	public TransportView toTransportView() {
	    TransportView transportView = new TransportView();
	
	    transportView.setId(id);
	    transportView.setOrigin(origin);
	    transportView.setDestination(destination);
	    transportView.setPrice(price);
	    transportView.setTransporterCompany(transporterCompany);
	    transportView.setState(state);
	
	    return transportView;
	}
}