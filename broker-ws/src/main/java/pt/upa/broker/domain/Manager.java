package pt.upa.broker.domain;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.*;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.cli.TransporterClient;

import javax.xml.registry.JAXRException;

public class Manager {
	static private final Logger log = LogManager.getRootLogger();
    private static Manager manager = new Manager();

    private UDDINaming uddiNaming;

    private int transportID = 0;
    private ArrayList<TransporterClient> transporterClients;
    private LinkedList<TransportOffers> transportOffers;

    private final ArrayList<String> knowCities = new ArrayList<>(Arrays.asList("Lisboa", "Leiria", "Santarém",
            "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda","Porto", "Braga", "Viana do Castelo",
            "Vila Real", "Bragança","Setúbal", "Évora", "Portalegre", "Beja","Faro"));


    private Manager() {
        transporterClients = new ArrayList<>();
        transportOffers = new LinkedList<>();
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


    public LinkedList<TransportOffers> getTransportOffers() {
    	return transportOffers;
    }

    public Transport getTransportById(String id){
    	for (TransportOffers t : transportOffers){
    		if (t.getTransport().getId().equals(id)){
    			return t.getTransport();
    		}
    	}
    	return null;
    }

    public void addTransportOffer(TransportOffers t){
    	transportOffers.add(t);
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

    public void requestTransport(String origin, String destination, int price)
            throws BadLocationFault_Exception, BadPriceFault_Exception, UnknownLocationFault_Exception,
            InvalidPriceFault_Exception, UnavailableTransportFault_Exception {

        validateTransport(origin, destination, price);
        pingTransporters();

        Transport transport = new Transport(
                getNextTransporterID(), origin, destination, price, null, TransportStateView.REQUESTED);
        TransportOffers transportOffer = new TransportOffers(transport, price);

        int count = 0;
        for (TransporterClient client : transporterClients) {
            JobView jobView = client.requestJob(origin, destination, price);
            if (jobView != null) {
                count++;
                transport.setState(TransportStateView.BUDGETED);
                transportOffer.addOffer(jobView);
                transportOffer.setTransporterClient(client);
            }
        }

        transportOffers.push(transportOffer);

        if (count == 0) {
            transport.setState(TransportStateView.FAILED);
            throwUnavailableTransportPriceFault_Exception (origin, destination);
        }
    }

    void decideOffers() throws BadJobFault_Exception, UnavailableTransportFault_Exception {

        for (TransportOffers t : transportOffers) {
            TransporterClient client = t.getTransporterClient();
            Transport transport = t.getTransport();
            transport.setState(TransportStateView.FAILED);

            int bestPrice = t.getReferencePrice();
            String bestJobID = null;
            for (JobView offer : t.getOffers() ) {
                int offerPrice = offer.getJobPrice();

                if (offerPrice < bestPrice) {
                    bestJobID = offer.getJobIdentifier();
                    bestPrice = offerPrice;
                } else client.decideJob(offer.getJobIdentifier(), false);
            }

            if (bestPrice < t.getReferencePrice()) {
                transport.setState(TransportStateView.BOOKED);
                client.decideJob(bestJobID, true);
            } else  {
                client.decideJob(bestJobID, false);
                throwUnavailableTransportPriceFault_Exception (transport.getOrigin(), transport.getDestination());
            }
        }
    }

    private void throwUnavailableTransportPriceFault_Exception (String origin, String destination)
            throws UnavailableTransportFault_Exception {
        UnavailableTransportFault faultInfo = new UnavailableTransportFault();
        faultInfo.setOrigin(origin);
        faultInfo.setDestination(destination);
        throw new UnavailableTransportFault_Exception(
                "There is no available source of transportation to the destination", faultInfo);

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
