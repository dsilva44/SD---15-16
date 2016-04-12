package pt.upa.broker.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.upa.broker.domain.Manager;

public class Manager {
	static private final Logger log = LogManager.getRootLogger();
    private static Manager manager = new Manager();
    
    public static Manager getInstance() { return manager; }
    
    
    public String requestTransport(String origin, String destination, int maxPrice){
    	return "";
    };
    
}
