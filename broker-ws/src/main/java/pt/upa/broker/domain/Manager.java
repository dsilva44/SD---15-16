package pt.upa.broker.domain;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.*;
import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.TransporterClient;

import javax.xml.registry.JAXRException;

import pt.upa.broker.exception.BrokerBadJobException;

public class Manager {
	static private final Logger log = LogManager.getRootLogger();
    private static Manager manager = new Manager();

    private UDDINaming uddiNaming;

    private int transportID = 0;
    private ArrayList<TransporterClient> transporterClients;
    private LinkedList<TransportOffer> allTransports;

    private final ArrayList<String> knowCities = new ArrayList<>(Arrays.asList("Lisboa", "Leiria", "Santarém",
            "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda","Porto", "Braga", "Viana do Castelo",
            "Vila Real", "Bragança","Setúbal", "Évora", "Portalegre", "Beja","Faro"));


    private Manager() {
        transporterClients = new ArrayList<>();
        allTransports = new LinkedList<>();
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


    public List<TransportOffer> getAllTransports() {
    	return allTransports;
    }

    public Transport getTransportById(String id){
    	for (TransportOffer t : allTransports){
    		if (t.getTransport().getId().equals(id)){
    			return t.getTransport();
    		}
    	}
    	return null;
    }

    public void addTransportOffer(TransportOffer t){
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

    public String requestTransport(String origin, String destination, int price)
            throws BadLocationFault_Exception, BadPriceFault_Exception, UnknownLocationFault_Exception,
            InvalidPriceFault_Exception, UnavailableTransportFault_Exception {

        validateTransport(origin, destination, price);
        pingTransporters();

        Transport transport = new Transport(
                getNextTransporterID(), origin, destination, price, null, TransportStateView.REQUESTED);
        TransportOffer transportOffer = new TransportOffer(transport, price);

        int count = 0;
        for (TransporterClient client : transporterClients) {
            JobView jobView = client.requestJob(origin, destination, price);
            if (jobView != null) {
                count++;
                transport.setState(TransportStateView.BUDGETED);
                transportOffer.addOffer(jobView);
            }
        }

        allTransports.push(transportOffer);

        if (count == 0) {
            transport.setState(TransportStateView.FAILED);
            UnavailableTransportFault faultInfo = new UnavailableTransportFault();
            faultInfo.setOrigin(origin);
            faultInfo.setDestination(destination);
            log.warn("There is no available transport from " + origin + "to " + destination);
            throw new UnavailableTransportFault_Exception(
                    "There is no available transport from " + origin + "to " + destination, faultInfo);
        }
        return transportOffer.getTransport().getId();
    }

    public String decideOffer(TransportOffer transportOffer) throws UnavailableTransportPriceFault_Exception {
        Transport transport = transportOffer.getTransport();
        transport.setState(TransportStateView.FAILED);

        String companyName = null;
        String bestJobID = null;
        TransporterClient client = null;
        int bestPrice = transportOffer.getReferencePrice();

        try {
            for (JobView offer : transportOffer.getOffers() ) {
                companyName = offer.getCompanyName();
                client = new TransporterClient(companyName);
                int offerPrice = offer.getJobPrice();

                if (offerPrice < bestPrice) {
                    bestJobID = offer.getJobIdentifier();
                    bestPrice = offerPrice;
                } else client.decideJob(offer.getJobIdentifier(), false);
            }

            if (client != null) {
                if (bestPrice < transportOffer.getReferencePrice()) {
                    transport.setState(TransportStateView.BOOKED);
                    transport.setTransporterCompany(companyName);
                    transportOffer.setChosenOfferID(bestJobID);
                    client.decideJob(bestJobID, true);
                }

                client.decideJob(bestJobID, false);
                UnavailableTransportPriceFault faultInfo = new UnavailableTransportPriceFault();
                faultInfo.setBestPriceFound(bestPrice);
                log.warn("the best price for transportation is " + bestPrice);
                throw new UnavailableTransportPriceFault_Exception(
                        "the best price for transportation is " + bestPrice, faultInfo);
            }
        } catch (BadJobFault_Exception e) {
            throw new BrokerBadJobException(
                    "Something went wrong while trying to decideJob on id: " + e.getFaultInfo().getId());
        } catch (Exception e) {
            log.error("Something went wrong while trying to create client stub", e);
        }

        return  transport.getId();
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
