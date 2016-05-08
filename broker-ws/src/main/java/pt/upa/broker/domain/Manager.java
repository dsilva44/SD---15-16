package pt.upa.broker.domain;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.broker.ws.*;
import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.TransporterClient;

import pt.upa.broker.exception.BrokerBadJobException;

public class Manager {
	static private final Logger log = LogManager.getRootLogger();
    private static Manager manager = new Manager();

    private EndpointManager epm;
    private Broker broker;
    private int transportID = 0;
    private ArrayList<TransporterClient> transporterClients;
    private ArrayList<Transport> transportsList;

    private final ArrayList<String> knowCities = new ArrayList<>(Arrays.asList("Lisboa", "Leiria", "Santarém",
            "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda","Porto", "Braga", "Viana do Castelo",
            "Vila Real", "Bragança","Setúbal", "Évora", "Portalegre", "Beja","Faro"));


    //Singleton
    private Manager() {
        transporterClients = new ArrayList<>();
        transportsList = new ArrayList<>();
    }

    //Singleton init
    public void init(EndpointManager endpointManager, Broker broker) {
        this.epm = endpointManager;
        this.broker = broker;
    }

    //getters
    public static Manager getInstance() { return manager; }
    ArrayList<TransporterClient> getTransporterClients() { return transporterClients; }
    public List<Transport> getTransportsList() {
        return transportsList;
    }
    public Broker getBroker() {return broker;}

    public EndpointManager getEndPointManager() {return epm;}

    Transport getTransportById(String id) {
        for (Transport t : transportsList)
            if (t.getId().equals(id))
                return t;
        return null;
    }



    String nextTransporterID() {
        String id = Integer.toString(transportID);
        transportID++;
        return id;
    }

    void addTransport(Transport t){
        transportsList.add(t);
    }

    private void replaceTransport(Transport oldT, Transport newT) {
        int index = transportsList.indexOf(oldT);
        transportsList.set(index, newT);
    }

    boolean updateTransportersList() {
        String query = "UpaTransporter%";
        ArrayList<String> transporterURLS = (ArrayList<String>) broker.uddiNamingList(query);

        transporterClients.clear();
        for (String url : transporterURLS) {
            TransporterClient client = new TransporterClient(url);
            transporterClients.add(client);
        }

        return !transporterClients.isEmpty();
    }

    public int findTransporters() {
        int count = 0;
        TransporterClient client;
        if (updateTransportersList()) {
            Iterator<TransporterClient> iterator = transporterClients.iterator();
            while(iterator.hasNext()) {
                try {
                    client = iterator.next();
                    client.ping(Integer.toString(count));
                    count++;
                } catch (Exception e) {
                    log.error("transporter is not available");
                    iterator.remove();
                }
            }
        }
        return count;
    }
    
    public  Transport updateTransportState(String id) throws UnknownTransportFault_Exception {
    	Transport t = getTransportById(id);
        if (t == null) throwUnknownTransportFault(id);

        assert t != null;
        TransporterClient client = new TransporterClient(broker.getUddiURL(), t.getTransporterCompany());
        JobView jobView = client.jobStatus(t.getChosenOfferID());
        t.setState(jobView);

        return t;
    }

    private void validateTransport(String origin, String destination, int price)
            throws UnknownLocationFault_Exception, InvalidPriceFault_Exception {

        if (!containsCaseInsensitive(origin, knowCities)) throwUnknownLocationFault(origin);
        else if (!containsCaseInsensitive(destination, knowCities)) throwUnknownLocationFault(destination);
        else if (price < 0) throwInvalidPriceFault(price);
    }

    public Transport requestTransport(String origin, String destination, int price)
            throws BadLocationFault_Exception, BadPriceFault_Exception, UnknownLocationFault_Exception,
            InvalidPriceFault_Exception, UnavailableTransportFault_Exception {

        validateTransport(origin, destination, price);
        Transport t = new Transport(nextTransporterID(), origin, destination, price, null, TransportStateView.REQUESTED);
        addTransport(t);

        findTransporters();
        boolean findT = false;
        for (TransporterClient client : transporterClients) {
            JobView jobView = client.requestJob(origin, destination, price);
            if (jobView != null) {
                t.setState(jobView);
                t.addOffer(jobView);
                findT = true;
            }
        }

        if (!findT) {
            t.setState(TransportStateView.FAILED);
            throwUnavailableTransportFault(origin, destination);
        }
        return t;
    }

    public Transport decideBestOffer(Transport transport) throws UnavailableTransportPriceFault_Exception {
        transport.setState(TransportStateView.FAILED);
        int bestPrice = transport.getPrice();

        JobView bestOffer = chooseBestOffer(transport);
        rejectOffersAcceptBest(transport, bestOffer);

        // nobody respond for reference price
        if (bestOffer == null) throwUnavailableTransportPriceFault(bestPrice);

        return transport;
    }

    public void clearTransports(){
        transportsList.clear();
    }

    public void clearTransportersClients(){
        transporterClients.forEach(TransporterClient::clearJobs);
        transporterClients.clear();
    }

    public void updateTransport(Transport newT) {
        Transport oldT = getTransportById(newT.getId());

        if (oldT == null) addTransport(newT);
        else replaceTransport(oldT, newT);
    }

    //-------------------------------------------Aux methods------------------------------------------------------------
    private boolean containsCaseInsensitive(String s, List<String> l) {
        for (String string : l){
            if (string.equalsIgnoreCase(s)){
                return true;
            }
        }
        return false;
    }

    private JobView chooseBestOffer(Transport t) {
        int bestPrice = t.getPrice();
        JobView bestOffer = null;

        for (JobView offer : t.getOffers() ) {
            int offerPrice = offer.getJobPrice();
            if (offerPrice < bestPrice || offerPrice == 0) {
                bestOffer = offer;
                bestPrice = offerPrice;
            }
        }
        return bestOffer;
    }

    private void rejectOffersAcceptBest(Transport t , JobView bestOffer) {
        for (JobView offer : t.getOffers()) {
            String companyName = offer.getCompanyName();
            TransporterClient client = new TransporterClient(broker.getUddiURL(), companyName);

            try {
                if (bestOffer != null && offer.getJobIdentifier().equals(bestOffer.getJobIdentifier()) ) {
                    t.acceptOffer(offer);
                    client.decideJob(offer.getJobIdentifier(), true);
                }
                else client.decideJob(offer.getJobIdentifier(), false);
            } catch (BadJobFault_Exception e) {
                throw new BrokerBadJobException(e.getMessage() + " -- id: " + e.getFaultInfo().getId());
            }
        }
    }

    //-------------------------------------------create Faults----------------------------------------------------------
    public void throwUnknownTransportFault(String id) throws UnknownTransportFault_Exception {
        UnknownTransportFault faultInfo = new UnknownTransportFault();
        faultInfo.setId(id);
        throw new UnknownTransportFault_Exception("Id unknown", faultInfo);
    }

    public void throwUnknownLocationFault(String location) throws UnknownLocationFault_Exception {
        UnknownLocationFault faultInfo = new UnknownLocationFault();
        faultInfo.setLocation(location);
        log.warn(location + " is a unknown location");
        throw new UnknownLocationFault_Exception(location + " is a unknown location", faultInfo);
    }

    public void throwInvalidPriceFault(int price) throws InvalidPriceFault_Exception {
        InvalidPriceFault faultInfo = new InvalidPriceFault();
        faultInfo.setPrice(price);
        log.warn(price + " is not valid");
        throw new InvalidPriceFault_Exception(price + " is not a valid price", faultInfo);
    }

    public void throwUnavailableTransportFault(String origin, String destination) throws UnavailableTransportFault_Exception {
        UnavailableTransportFault faultInfo = new UnavailableTransportFault();
        faultInfo.setOrigin(origin);
        faultInfo.setDestination(destination);
        throw new UnavailableTransportFault_Exception(
                "There is no available transport from " + origin + " to " + destination, faultInfo);
    }

    private void throwUnavailableTransportPriceFault(int price) throws UnavailableTransportPriceFault_Exception {
        UnavailableTransportPriceFault faultInfo = new UnavailableTransportPriceFault();
        faultInfo.setBestPriceFound(price);
        throw new UnavailableTransportPriceFault_Exception(
                "the best price for transportation is " + price, faultInfo);
    }
}
