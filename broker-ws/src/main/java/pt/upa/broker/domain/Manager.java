package pt.upa.broker.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.Exception.CannotUpdateTransportersClientsException;
import pt.upa.transporter.ws.cli.TransporterClient;

import javax.xml.registry.JAXRException;

public class Manager {
	static private final Logger log = LogManager.getRootLogger();
    private static Manager manager = new Manager();

    private String uddiURL = "http://localhost:9090";
    private UDDINaming uddiNaming;

    private String query = "UpaTransporter%";
    private ArrayList<TransporterClient> transporterClients;
    private ArrayList<Transport> bookedTransports;


    private Manager() {
        transporterClients = new ArrayList<>();
        bookedTransports = new ArrayList<>();
    }

    public void init(String uddiURL) {
        try {
            uddiNaming = new UDDINaming(uddiURL);
        } catch (JAXRException e) {
            log.error("Cannot initialize uddi", e);
        }
    }
    
    public static Manager getInstance() { return manager; }

    ArrayList<TransporterClient> getTransporterClients() { return transporterClients; }

    void setUddiNaming(UDDINaming uddiNaming) { this.uddiNaming = uddiNaming; }

    public boolean updateTransportersList() {
        ArrayList<TransporterClient> rollBack = transporterClients;
        transporterClients.clear();
        try {
            ArrayList<String> endpoints = (ArrayList<String>) uddiNaming.list(query);
            for (String endpoint : endpoints) {
                TransporterClient client = new TransporterClient(uddiURL, endpoint);
                transporterClients.add(client);
            }
            return !transporterClients.isEmpty();

        } catch (Exception e) {
            log.error("something goes wrong while updating transporters clients", e);
            transporterClients = rollBack;
            throw new CannotUpdateTransportersClientsException();
        }
    }

    public int pingTransporters() {
        int count = 0;
        if (updateTransportersList()) {
            for (TransporterClient client : transporterClients) {
                try {
                    client.ping(Integer.toString(count));
                    count++;
                } catch (Exception e) {
                    log.error("something goes wrong while contacting transporter client", e);
                }
            }
        }

        return count;
    }
    
    public ArrayList<Transport> getBookedTransports(){
    	return bookedTransports;
    }
    
    public Transport getTransportById(String id){
    	for (Transport t : bookedTransports){
    		if (t.getId().equals(id)){
    			return t;
    		}
    	}
    	return null;
    }
    
    public void addTransport(Transport t){
    	bookedTransports.add(t);
    }
    
}
