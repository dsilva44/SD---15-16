package pt.upa.broker.domain;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportStateView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;

public class ManagerTest {

    // static members

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() {
    }

    @AfterClass
    public static void oneTimeTearDown() {

    }


    // members
    private Manager manager = Manager.getInstance();

    private Transport validTransport = new Transport("id1", "Lisboa", "Leiria", 50, "UpaTransporter1", TransportStateView.REQUESTED);
    private Transport invalidTransport = new Transport( "id2", "Lisboa", "Leiria",  50, "UpaTransporter2", TransportStateView.REQUESTED);

    private final ArrayList<String> centro = new ArrayList<>(Arrays.asList("Lisboa", "Leiria", "Santarém",
            "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda"));
    private final ArrayList<String> norte = new ArrayList<>(Arrays.asList("Porto", "Braga", "Viana do Castelo",
            "Vila Real", "Bragança"));
    private final ArrayList<String> sul = new ArrayList<>(Arrays.asList("Setúbal", "Évora", "Portalegre", "Beja",
            "Faro"));

    private String centroLocation1 = "Lisboa";
    private String centroLocation2 = "Leiria";
    private String unknownLocation = "BATATA";
    private String oddLocation = "Faro";
    private String evenLocation = "Braga";
    private int validPrice = 20;
    private int invalidPrice = -1;

    // initialization and clean-up for each test
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    @Test(expected= UnknownLocationFault_Exception.class)
    public void failUnknownOrigin(){
    	manager.requestTransport(unknownLocation, centroLocation1, validPrice);
    }
    
    @Test(expected= UnknownLocationFault_Exception.class)
    public void failUnknownDestination(){
    	manager.requestTransport(centroLocation1,unknownLocation, validPrice);
    }
    
    @Test(expected= InvalidPriceFault_Exception.class)
    public void failInvalidPrice(){
    	manager.requestTransport(centroLocation1,centroLocation2, invalidPrice);
    }
    
    @Test(expected= UnavailableTransportFault_Exception.class)
    public void failUnavailableTransport(){
    	
    }
    @Test(expected= UnavailableTransportPriceFault_Exception.class)
    public void failUnavailableTransportPrice(){
    	
    }
    @Test
    public void sucess(){
    	
    }
    
    
    
    
    
    
    
    
}
