package pt.upa.broker.domain;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pt.upa.broker.ws.TransportStateView;
import pt.upa.broker.ws.TransportView;


public class TransportTest {
    // static members

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() {
    }

    @AfterClass
    public static void oneTimeTearDown() {

    }


    // members

    // initialization and clean-up for each test
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void successTransportViewConversion() {
    	Transport trans = new Transport("id","Lisboa","Leiria", 20, "UpaTransporter1", TransportStateView.REQUESTED);
    	
        TransportView transportView = new TransportView();
        transportView.setId("id");
        transportView.setOrigin("Lisboa");
        transportView.setDestination("Leiria");
        transportView.setPrice(20);
        transportView.setTransporterCompany("UpaTransporter1");
        transportView.setState(TransportStateView.REQUESTED);

        TransportView transportViewConversion = trans.toTransportView();

        assertEquals("TransportIdentifier is wrong", transportView.getId(), transportViewConversion.getId());
        assertEquals("TransportOrigin is wrong",transportView.getOrigin(), transportViewConversion.getOrigin());
        assertEquals("TransportDestination is wrong", transportView.getDestination(), transportViewConversion.getDestination());
        assertEquals("TransportPrice is wrong", transportView.getPrice(), transportViewConversion.getPrice());
        assertEquals("CompanyName is wrong", transportView.getTransporterCompany(), transportViewConversion.getTransporterCompany());
        assertEquals("TransportState is wrong", transportView.getState(), transportViewConversion.getState());
    }

}