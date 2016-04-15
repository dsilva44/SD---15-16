package pt.upa.broker.domain;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.exception.BrokerUddiNamingException;
import pt.upa.broker.ws.*;
import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.TransporterClient;

import javax.xml.registry.JAXRException;

import pt.upa.broker.exception.BrokerBadJobException;

public class Manager {
	static private final Logger log = LogManager.getRootLogger();
    private static Manager manager = new Manager();

    private UDDINaming uddiNaming;
    private String uddURL;
    private int transportID = 0;
    private ArrayList<TransporterClient> transporterClients;
    private LinkedList<Transport> allTransports;

    private final ArrayList<String> knowCities = new ArrayList<>(Arrays.asList("Lisboa", "Leiria", "Santarém",
            "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda","Porto", "Braga", "Viana do Castelo",
            "Vila Real", "Bragança","Setúbal", "Évora", "Portalegre", "Beja","Faro"));


    private Manager() {
        transporterClients = new ArrayList<>();
        allTransports = new LinkedList<>();
    }

    public void init(String uddiURL) {
        this.uddURL = uddiURL;
        try {
            this.uddiNaming = new UDDINaming(uddiURL);
        } catch (JAXRException e) {
            throw new BrokerUddiNamingException("Cannot Create uddiNaming instance");
        }
    }
    
    public static Manager getInstance() { return manager; }

    String getNextTransporterID() {
        String id = Integer.toString(transportID);
        transportID++;
        return id;
    }

    ArrayList<TransporterClient> getTransporterClients() { return transporterClients; }

    public void setUddiNaming(UDDINaming uddiNaming) { this.uddiNaming = uddiNaming; }


    public boolean updateTransportersList() {
        try {
            String query = "UpaTransporter%";
            ArrayList<String> endpoints = (ArrayList<String>) uddiNaming.list(query);
            transporterClients.clear();
            for (String endpoint : endpoints) {
                TransporterClient client = new TransporterClient(endpoint);
                transporterClients.add(client);
            }

        } catch (JAXRException e) {
            log.error("something goes wrong whit uddiNaming", e);
        }
        return !transporterClients.isEmpty();
    }

    public int pingTransporters() {
        int count = 0;
        TransporterClient client = null;
        if (updateTransportersList()) {
            Iterator<TransporterClient> iterator = transporterClients.iterator();
            while(iterator.hasNext()) {
                try {
                    client = iterator.next();
                    client.ping(Integer.toString(count));
                    count++;
                } catch (Exception e) {
                    log.error(client.getWsURL() + " is not available");
                    iterator.remove();
                }
            }
        }
        return count;
    }


    public List<Transport> getAllTransports() {
    	return allTransports;
    }

    public Transport getTransportById(String id){
    	for (Transport t : allTransports){
    		if (t.getId().equals(id)){
    			return t;
    		}
    	}
    	return null;
    }

    void addTransport(Transport t){
    	allTransports.add(t);
    }

    public void validateTransport(String origin, String destination, int price)
            throws UnknownLocationFault_Exception, InvalidPriceFault_Exception {
        class UnknownLocation {
            private void throwException(String location) throws UnknownLocationFault_Exception {
                UnknownLocationFault faultInfo = new UnknownLocationFault();
                faultInfo.setLocation(location);
                log.warn(location + " is a unknown location");
                throw new UnknownLocationFault_Exception(location + " is a unknown location", faultInfo);
            }
        }

        if (!containsCaseInsensitive(origin, knowCities)) new UnknownLocation().throwException(origin);
        if (!containsCaseInsensitive(destination, knowCities)) new UnknownLocation().throwException(destination);
        if (price < 0) {
            InvalidPriceFault faultInfo = new InvalidPriceFault();
            faultInfo.setPrice(price);
            log.warn(price + " is not valid");
            throw new InvalidPriceFault_Exception(price + " is not a valid price", faultInfo);
        }
    }

    public Transport requestTransport(String origin, String destination, int price)
            throws BadLocationFault_Exception, BadPriceFault_Exception, UnknownLocationFault_Exception,
            InvalidPriceFault_Exception, UnavailableTransportFault_Exception {

        validateTransport(origin, destination, price);
        pingTransporters();

        Transport transport = new Transport(
                getNextTransporterID(), origin, destination, price, null, TransportStateView.REQUESTED);

        int count = 0;
        for (TransporterClient client : transporterClients) {
            JobView jobView = client.requestJob(origin, destination, price);
            if (jobView != null) {
                count++;
                transport.setState(TransportStateView.BUDGETED);
                transport.addOffer(jobView);
            }
        }

        addTransport(transport);

        if (count == 0) {
            transport.setState(TransportStateView.FAILED);
            UnavailableTransportFault faultInfo = new UnavailableTransportFault();
            faultInfo.setOrigin(origin);
            faultInfo.setDestination(destination);
            log.warn("There is no available transport from " + origin + "to " + destination);
            throw new UnavailableTransportFault_Exception(
                    "There is no available transport from " + origin + "to " + destination, faultInfo);
        }
        return transport;
    }

    public Transport decideBestOffer(Transport transport) throws UnavailableTransportPriceFault_Exception {
        transport.setState(TransportStateView.FAILED);

        String bestJobID = null;
        int bestPrice = transport.getPrice();

        try {
            // Choose best offer
            for (JobView offer : transport.getOffers() ) {
                int offerPrice = offer.getJobPrice();

                if (offerPrice < bestPrice) {
                    bestJobID = offer.getJobIdentifier();
                    bestPrice = offerPrice;
                }
            }

            //Reject offers
            for (JobView offer : transport.getOffers()) {
                String companyName = offer.getCompanyName();
                TransporterClient client = new TransporterClient(uddURL, companyName);

                if (offer.getJobIdentifier().equals(bestJobID) & bestPrice < transport.getPrice()) {
                    transport.setState(TransportStateView.BOOKED);
                    transport.setTransporterCompany(companyName);
                    transport.setChosenOfferID(bestJobID);
                    client.decideJob(bestJobID, true);
                }
                else client.decideJob(offer.getJobIdentifier(), false);
            }

            if (bestJobID == null)  {
                UnavailableTransportPriceFault faultInfo = new UnavailableTransportPriceFault();
                faultInfo.setBestPriceFound(bestPrice);
                log.warn("the best price for transportation is " + bestPrice);
                throw new UnavailableTransportPriceFault_Exception(
                        "the best price for transportation is " + bestPrice, faultInfo);
            }
        } catch (BadJobFault_Exception e) {
            throw new BrokerBadJobException(e.getMessage() + " -- id: " + e.getFaultInfo().getId());
        } catch (JAXRException e) {
            log.error("Something went wrong while trying to create transporter stub");
        }

        return transport;
    }

    private boolean containsCaseInsensitive(String s, List<String> l) {
        for (String string : l){
            if (string.equalsIgnoreCase(s)){
                return true;
            }
        }
        return false;
    }

}
