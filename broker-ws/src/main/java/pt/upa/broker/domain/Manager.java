package pt.upa.broker.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.cli.TransporterClient;

import javax.xml.registry.JAXRException;

public class Manager {
	static private final Logger log = LogManager.getRootLogger();
    private static Manager manager = new Manager();

    private UDDINaming uddiNaming;

    private ArrayList<TransporterClient> transporterClients;
    private ArrayList<Transport> bookedTransports;


    private Manager() {
        transporterClients = new ArrayList<>();
        bookedTransports = new ArrayList<>();
    }
    
    public static Manager getInstance() { return manager; }

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
