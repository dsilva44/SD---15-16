package pt.upa.broker.domain;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.broker.ws.*;
import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.TransporterClient;

public class Manager {
	static private final Logger log = LogManager.getRootLogger();
    private static Manager manager = new Manager();

    private EndpointManager epm;
    private Broker currBroker;
    private List<TransporterClient> transporterClients;
    private List<Transport> transportsList;
    private Map<String, String> transportResponses;

    private String ksPath;
    private String password;

    private final List<String> knowCities = new ArrayList<>(Arrays.asList("Lisboa", "Leiria", "Santarém",
            "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda","Porto", "Braga", "Viana do Castelo",
            "Vila Real", "Bragança","Setúbal", "Évora", "Portalegre", "Beja","Faro"));

    //Singleton
    private Manager() {
        transporterClients = new ArrayList<>();
        transportsList = new ArrayList<>();
        transportResponses = new HashMap<>();
    }

    //Singleton init
    public void init(EndpointManager endpointManager, Broker broker, String path, String pass ) {
        this.epm = endpointManager;
        this.currBroker = broker;
        this.ksPath = path;
        this.password = pass;
        broker.monitor(2000, 2000);
    }

    //State pattern
    void goNext() {
        currBroker.goNext();
    }

    //getters
    public static Manager getInstance() { return manager; }
    List<TransporterClient> getTransporterClients() { return transporterClients; }
    public List<Transport> getTransportsList() {
        return transportsList;
    }
    public Map<String, String> getTransportResponses() { return transportResponses; }

    public Broker getCurrBroker() {return currBroker;}
    public EndpointManager getEndPointManager() { return epm; }

    void setCurrBroker(Broker currBroker) { this.currBroker = currBroker; }

    Transport getTransportById(String id) {
        for (Transport t : transportsList)
            if (t.getId().equals(id))
                return t;
        return null;
    }

    private String nextTransporterID() {
        return Integer.toString(getTransportsList().size());
    }

    void addTransport(Transport t){
        transportsList.add(t);
    }

    void replaceTransport(Transport oldT, Transport newT) {
        int index = transportsList.indexOf(oldT);
        transportsList.set(index, newT);
    }

    boolean updateTransportersList() {
        String query = "UpaTransporter%";
        ArrayList<String> transporterURLS = (ArrayList<String>) epm.uddiList(query);

        transporterClients.clear();
        for (String url : transporterURLS) {
            TransporterClient client = new TransporterClient(url, epm.getWsName(), ksPath, password);
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

        TransporterClient client = new TransporterClient(epm.getUddiURL(),
                t.getTransporterCompany(), epm.getWsName(), ksPath, password);
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

    public void clearTransportersClients(){
        transporterClients.forEach(TransporterClient::clearJobs);
        transporterClients.clear();
    }

    public void updateTransport(TransportView transportView, String chosenOfferID, String oprID, String res) {
        Transport newTransport = new Transport(transportView, chosenOfferID);
        Transport oldTransport = manager.getTransportById(newTransport.getId());

        if (oldTransport != null) {
            manager.replaceTransport(oldTransport, newTransport);
            log.debug("Update: " + newTransport.toString());
        } else {
            manager.addTransport(newTransport);
            log.debug("Create: "+newTransport.toString());
        }

        if (oprID != null)
            getTransportResponses().put(oprID, res);
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
            TransporterClient client = new TransporterClient(epm.getUddiURL(), companyName, epm.getWsName(), ksPath, password);

            try {
                if (bestOffer != null && offer.getJobIdentifier().equals(bestOffer.getJobIdentifier()) ) {
                    t.acceptOffer(offer);
                    client.decideJob(offer.getJobIdentifier(), true);
                }
                else client.decideJob(offer.getJobIdentifier(), false);
            } catch (BadJobFault_Exception e) {
                log.warn(e.getMessage() + " -- id: " + e.getFaultInfo().getId());
            }
        }
        t.clearOffers();
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
