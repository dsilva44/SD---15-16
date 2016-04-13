package pt.upa.broker.domain;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import javax.xml.registry.JAXRException;

public class Manager {
	static private final Logger log = LogManager.getRootLogger();
    private static Manager manager = new Manager();

    private UDDINaming uddiNaming;

    private ArrayList<String> availTransporters;
    private ArrayList<Transport> bookedTransports;


    private Manager() {
        availTransporters = new ArrayList<>();
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

    public ArrayList<String> getAvailTransporters() { return availTransporters; }

    void setUddiNaming(UDDINaming uddiNaming) { this.uddiNaming = uddiNaming; }

    public boolean updateTransportersList() {
        //TODO
        return false;
    }

    public int pingTransporters() {
        //TODO
        return 0;
    }
    
}
