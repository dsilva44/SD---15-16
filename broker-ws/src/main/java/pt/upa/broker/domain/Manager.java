package pt.upa.broker.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.upa.broker.domain.Manager;

public class Manager {
	static private final Logger log = LogManager.getRootLogger();
    private static Manager manager = new Manager();
    
    private ArrayList<Transport> transports;
    
    public static Manager getInstance() { return manager; }
    
    public String requestTransport(String origin, String destination, int maxPrice){
    	return "";
    }

	public List<Transport> listTransports() {
		return transports;		
	}
    
}
