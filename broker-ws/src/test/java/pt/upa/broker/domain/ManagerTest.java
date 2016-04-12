package pt.upa.broker.domain;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
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

    private Transport validJob = new Transport("UpaTransporter1", "validjobtest", "Lisboa", "Leiria", 50, JobStateView.PROPOSED);
    private Transport invalidJob = new Transport( "UpaTransporter1", "invalidjobtest", "Lisboa", "Leiria", 50, JobStateView.HEADING);

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

    // initialization and clean-up for each test
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    @Test(expected= UnknownLocationFault_Exception.class)
    public void failUnknownOrigin(){
    	
    }
    
    @Test(expected= UnknownLocationFault_Exception.class)
    public void failUnknownDestination(){
    	
    }
    
    @Test(expected= InvalidPriceFault_Exception.class)
    public void failInvalidPrice(){
    	
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
