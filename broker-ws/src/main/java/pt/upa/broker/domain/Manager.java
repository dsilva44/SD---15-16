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

    //private UDDINaming uddiNaming;
    private String uddiURL;
    private String keyStorePath;
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
    public void init(String uddiURL, String keyStorePath) {
        this.uddiURL = uddiURL;
        this.keyStorePath = keyStorePath;
    }

    //getters
    public String getKeyStorePath(){
        return keyStorePath;
    }
    public static Manager getInstance() { return manager; }
    ArrayList<TransporterClient> getTransporterClients() { return transporterClients; }
    public List<Transport> getTransportsList() {
        return transportsList;
    }

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

    boolean updateTransportersList(String uddiURL) {
        try {
            String query = "UpaTransporter%";
            UDDINaming uddiNaming = new UDDINaming(uddiURL);
            ArrayList<String> transporterURLS = (ArrayList<String>) uddiNaming.list(query);
            transporterClients.clear();
            for (String url : transporterURLS) {
                TransporterClient client = new TransporterClient(url);
                transporterClients.add(client);
            }
        } catch (JAXRException e) {
            log.error("something goes wrong whit uddiNaming", e);
        }
        return !transporterClients.isEmpty();
    }

    public int findTransporters() {
        int count = 0;
        TransporterClient client;
        if (updateTransportersList(uddiURL)) {
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

        TransporterClient client = new TransporterClient(uddiURL, t.getTransporterCompany());
        JobView jobView = client.jobStatus(t.getChosenOfferID());
        t.nextState(jobView);

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
        findTransporters();

        Transport transport = new Transport(
                nextTransporterID(), origin, destination, price, null, TransportStateView.REQUESTED);

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
            log.warn("There is no available transport from " + origin + " to " + destination);
            throw new UnavailableTransportFault_Exception(
                    "There is no available transport from " + origin + " to " + destination, faultInfo);
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

                if (offerPrice < bestPrice || offerPrice == 0) {
                    bestJobID = offer.getJobIdentifier();
                    bestPrice = offerPrice;
                }
            }

            //Reject offers
            for (JobView offer : transport.getOffers()) {
                String companyName = offer.getCompanyName();
                TransporterClient client = new TransporterClient(uddiURL, companyName);

                if (offer.getJobIdentifier().equals(bestJobID) ) {
                    transport.setState(TransportStateView.BOOKED);
                    transport.setTransporterCompany(companyName);
                    transport.setChosenOfferID(bestJobID);
                    transport.setPrice(bestPrice);
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


    public void clearTransports(){
        transportsList.clear();
    }

    public void clearTransportersClients(){
        transporterClients.forEach(TransporterClient::clearJobs);
        transporterClients.clear();
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
}
