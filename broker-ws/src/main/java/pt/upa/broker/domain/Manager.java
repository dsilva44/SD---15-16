package pt.upa.broker.domain;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Manager {
	static private final Logger log = LogManager.getRootLogger();
    private static Manager manager = new Manager();

    private ArrayList<Transport> transports;

    private Manager() {
    }
    
    public static Manager getInstance() { return manager; }
    
}
