package pt.upa.broker.domain;

import mockit.*;
import org.junit.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.*;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.cli.TransporterClient;

import javax.xml.registry.JAXRException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.*;



public class ManagerTest {

    // static members

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() {
    }

    @AfterClass
    public static void oneTimeTearDown() {

    }

    //Members
    private Manager manager = Manager.getInstance();
    private String transporterQuery = "UpaTransporter%";
    private String wsURL1 = "http://localhost:8081/transporter-ws/endpoint";
    private String wsURL2 = "http://localhost:8082/transporter-ws/endpoint";
    private Collection<String> endpointsList = new ArrayList<>(Arrays.asList(wsURL1, wsURL2));
    private ArrayList<String> emptyList = new ArrayList<>();

    private String centroCity1 = "LisBOA";
    private String centroCity2 = "LeIRia";

    private JobView offer1, offer2, offer3, offer4;
    private Transport transport1, transport2;

    // initialization and clean-up for each test
    @Before
    public void setUp() {
        offer1 = new JobView(); offer1.setJobIdentifier("1");
        offer2 = new JobView(); offer2.setJobIdentifier("2");
        offer3 = new JobView(); offer3.setJobIdentifier("3");
        offer4 = new JobView(); offer4.setJobIdentifier("4");

        transport1 = new Transport(); transport1.setId("1"); transport1.setState(TransportStateView.BUDGETED);
        transport2 = new Transport(); transport2.setId("2"); transport2.setState(TransportStateView.BUDGETED);
    }

    @After
    public void tearDown() {
        manager.getTransportOffers().clear();
    }

    // --------------------------------------- updateTransporters ------------------------------------------------------
    @Test
    public void successUpdateTransportersList(@Mocked UDDINaming uddiNamingMock)
            throws JAXRException {
        new Expectations() {{
            uddiNamingMock.list(transporterQuery); result = endpointsList;
        }};
        manager.setUddiNaming(uddiNamingMock);

        boolean result = manager.updateTransportersList();

        new Verifications() {{
            uddiNamingMock.list(transporterQuery); maxTimes = 1;
        }};

        assertFalse("transporters list is empty", manager.getTransporterClients().isEmpty());
        assertTrue("must return true", result);
    }

    @Test
    public void uddiReturnEmptyListShouldReturnFalse(@Mocked UDDINaming uddiNamingMock) throws JAXRException {
        new Expectations() {{
            uddiNamingMock.list(transporterQuery); result = emptyList;
        }};
        manager.setUddiNaming(uddiNamingMock);

        boolean result = manager.updateTransportersList();

        new Verifications() {{
            uddiNamingMock.list(transporterQuery); maxTimes = 1;
        }};

        assertTrue("transporters list is not empty", manager.getTransporterClients().isEmpty());
        assertFalse("must return false", result);
    }
    // ----------------------------------------- pingTransporters ------------------------------------------------------

    @Test
    public void successPingAllTransporters(@Mocked UDDINaming uddiNamingMock, @Mocked TransporterClient transporterClientMock)
            throws JAXRException {
        new Expectations() {{
            uddiNamingMock.list(transporterQuery); result = endpointsList;
            transporterClientMock.ping("0"); result = "Pong 0!";
            transporterClientMock.ping("1"); result = "Pong 1!";
        }};
        manager.setUddiNaming(uddiNamingMock);

        int result = manager.pingTransporters();

        new Verifications() {{
            transporterClientMock.ping("0"); maxTimes = 1;
            transporterClientMock.ping("1"); maxTimes = 1;
            uddiNamingMock.list(transporterQuery); maxTimes = 2;
        }};

        assertTrue("must return 2", result == 2);
    }

    @Test
    public void pingEmptyTransportersShouldReturn0(@Mocked UDDINaming uddiNamingMock) throws JAXRException {
        new Expectations() {{
            uddiNamingMock.list(transporterQuery); result = emptyList;
        }};
        manager.setUddiNaming(uddiNamingMock);

        int result = manager.pingTransporters();

        new Verifications() {{
            uddiNamingMock.list(transporterQuery); maxTimes = 1;
        }};

        assertTrue("must return 0", result == 0);
    }

    @Test
    public void successPingSomeTransportersAndDeleteBrokenReferences(
            @Mocked UDDINaming uddiNamingMock, @Mocked TransporterClient transporterClientMock) throws JAXRException {
        new Expectations() {{
            uddiNamingMock.list(transporterQuery); result = endpointsList;
            transporterClientMock.ping("0"); result = "Pong 0!";
            transporterClientMock.ping("1"); result = new JAXRException();
        }};
        manager.setUddiNaming(uddiNamingMock);

        int result = manager.pingTransporters();

        new Verifications() {{
            uddiNamingMock.list(transporterQuery); maxTimes = 2;
            transporterClientMock.ping("0"); maxTimes = 1;
            transporterClientMock.ping("1"); maxTimes = 1;
        }};

        assertTrue("must return 1", result == 1);
        assertTrue("broken client  array", manager.getTransporterClients().size() == 1);
    }

    //--------------------------------------------getTransportByID------------------------------------------------------

    @Test
	public void successGetTransportNonExisting(){
		assertNull(manager.getTransportById("invalidID"));
	}

    @Test
	public void successGetTransportExisting() {
		Transport t1 = new Transport();
		t1.setId("id1");
        TransportOffers transportOffers = new TransportOffers(t1, 0);
		manager.addTransportOffer(transportOffers);

		assertEquals(t1, manager.getTransportById("id1"));
	}

    @Test
    public void getNextTransporterIdShouldReturnDifferentValues() {
        String id1 = manager.getNextTransporterID();
        String id2 = manager.getNextTransporterID();
        String id3 = manager.getNextTransporterID();

        assertNotEquals("ids are equals", id1, id2);
        assertNotEquals("ids are equals", id1, id3);
        assertNotEquals("ids are equals", id2, id3);
    }
    //-------------------------------------requestTransport(origin, destination, price)---------------------------------
    @Test(expected = UnknownLocationFault_Exception.class)
    public void unknownOrigin()
            throws  Exception{
        String invalidDestination = "PoTATO";
        int price = 50;

        manager.requestTransport(invalidDestination, centroCity1, price);
    }

    @Test(expected = UnknownLocationFault_Exception.class)
    public void unknownDestination()
            throws  Exception {
        String invalidOrigin = null;
        int price = 50;

        manager.requestTransport(centroCity2, invalidOrigin, price);
    }

    @Test(expected = InvalidPriceFault_Exception.class)
    public void invalidPrice()
            throws InvalidPriceFault_Exception, BadLocationFault_Exception, UnknownLocationFault_Exception,
            BadPriceFault_Exception, UnavailableTransportFault_Exception {
        int price = -50;

        manager.requestTransport(centroCity1, centroCity2, price);
    }

    @Test
    public void someTransportersResponse(
            @Mocked UDDINaming uddiNamingMock, @Mocked TransporterClient transporterClientMock)
            throws  Exception {
        int referencePrice = 50;

        new Expectations() {{
            uddiNamingMock.list(transporterQuery); result = endpointsList;
            transporterClientMock.requestJob(centroCity1, centroCity2, referencePrice);
            result = null;
            transporterClientMock.requestJob(centroCity1, centroCity2, referencePrice);
            result = new JobView();
        }};

        manager.requestTransport(centroCity1, centroCity2, referencePrice);

        new Verifications() {{
            uddiNamingMock.list(transporterQuery); maxTimes = 1;
            transporterClientMock.requestJob(centroCity1, centroCity2, referencePrice); maxTimes = 2;
        }};

        assertEquals("transporter offer not saved, different size", 1, manager.getTransportOffers().size());
        TransportOffers transportOffers = manager.getTransportOffers().get(0);
        Transport transport = transportOffers.getTransport();
        TransporterClient transporterClient = transportOffers.getTransporterClient();
        assertEquals("state don't changed", transport.getState(), TransportStateView.BUDGETED);
        assertNotNull("client not associated", transporterClient);
    }

    @Test(expected = UnavailableTransportFault_Exception.class)
    public void allNullTransportersResponse(@Mocked UDDINaming uddiNamingMock, @Mocked TransporterClient transporterClientMock)
            throws  Exception{
        int referencePrice = 200;

        new Expectations() {{
            uddiNamingMock.list(transporterQuery); result = endpointsList;
            transporterClientMock.requestJob(centroCity1, centroCity2, referencePrice);
            result = null ;
            transporterClientMock.requestJob(centroCity1, centroCity2, referencePrice);
            result = null ;
        }};

        try {
            manager.requestTransport(centroCity1, centroCity2, referencePrice);
        } catch (UnavailableTransportFault_Exception e) {

            assertTrue("transporter offer not saved", manager.getTransportOffers().size() == 1);

            for (TransportOffers t : manager.getTransportOffers()) {
                TransportStateView state = t.getTransport().getState();
                assertEquals("wrong state", state, TransportStateView.FAILED);
                assertNull("wrong client associated", t.getTransporterClient());
            }

            throw new UnavailableTransportFault_Exception(null, null);

        }
    }
    //-------------------------------------------decideOffers()----------------------------------------------------------
    @Test
    public void someOffersBelowReferencePrice(
            @Mocked TransporterClient transporterClientMock) throws  Exception {
        int referencePrice = 10;
        offer1.setJobPrice(9); offer2.setJobPrice(10); offer3.setJobPrice(15); offer4.setJobPrice(5);
        TransportOffers transportOffers1 = new TransportOffers(transport1, referencePrice);
        TransportOffers transportOffers2 = new TransportOffers(transport2, referencePrice);
        transportOffers1.addOffer(offer1); transportOffers1.addOffer(offer2);
        transportOffers2.addOffer(offer3); transportOffers2.addOffer(offer4);
        transportOffers1.setTransporterClient(transporterClientMock);
        transportOffers2.setTransporterClient(transporterClientMock);

        manager.addTransportOffer(transportOffers1); manager.addTransportOffer(transportOffers2);

        new Expectations() {{
            transporterClientMock.decideJob("1", true); result = new JobView();
            transporterClientMock.decideJob("2", false); result = new JobView();
            transporterClientMock.decideJob("3", false); result = new JobView();
            transporterClientMock.decideJob("4", true); result = new JobView();
        }};

        manager.decideOffers();

        new Verifications() {{
            transporterClientMock.decideJob("1", true); maxTimes = 1;
            transporterClientMock.decideJob("2", false); maxTimes = 1;
            transporterClientMock.decideJob("3", false); maxTimes = 1;
            transporterClientMock.decideJob("4", true); maxTimes = 1;
        }};

        assertTrue("transporter offer not saved", manager.getTransportOffers().size() == 2);
        TransportStateView state1 = manager.getTransportById("1").getState();
        TransportStateView state2 = manager.getTransportById("2").getState();

        assertEquals("wrong STATE", state1, TransportStateView.BOOKED);
        assertEquals("wrong STATE", state2, TransportStateView.BOOKED);
    }

    @Test(expected = UnavailableTransportPriceFault_Exception.class)
    public void allOffersAboveReferencePrice(
            @Mocked TransporterClient transporterClientMock) throws  Exception {
        int referencePrice = 15;
        offer1.setJobPrice(15); offer2.setJobPrice(16);
        TransportOffers transportOffers = new TransportOffers(transport1, referencePrice);
        transportOffers.addOffer(offer1);
        transportOffers.addOffer(offer2);
        transportOffers.setTransporterClient(transporterClientMock);
        manager.addTransportOffer(transportOffers);

        new Expectations() {{
            transporterClientMock.decideJob("1", false); result = new JobView();
            transporterClientMock.decideJob("2", false); result = new JobView();
        }};

        try {
            manager.decideOffers();

        } catch (UnavailableTransportPriceFault_Exception e) {
            assertTrue("transporter offer not saved", manager.getTransportOffers().size() == 1);
            TransportStateView state = manager.getTransportById("1").getState();

            assertEquals("wrong SATE", state, TransportStateView.FAILED);

            throw new UnavailableTransportPriceFault_Exception(null, null);
        }
    }

    @Test
    public void offersSamePriceBelowReferencePrice(
            @Mocked TransporterClient transporterClientMock) throws  Exception {
        int referencePrice = 15;
        offer1.setJobPrice(14); offer2.setJobPrice(14);
        TransportOffers transportOffers = new TransportOffers(transport1, referencePrice);
        transportOffers.addOffer(offer1); transportOffers.addOffer(offer2);
        transportOffers.setTransporterClient(transporterClientMock);
        manager.addTransportOffer(transportOffers);

        new Expectations() {{
            transporterClientMock.decideJob("1", true); result = new JobView();
            transporterClientMock.decideJob("2", false); result = new JobView();
        }};

        manager.decideOffers();

        new Verifications() {{
            transporterClientMock.decideJob("1", true); maxTimes = 1;
            transporterClientMock.decideJob("2", false); maxTimes = 2;
        }};

        assertTrue("transporter offer not saved", manager.getTransportOffers().size() == 1);
        TransportStateView state = manager.getTransportById("1").getState();

        assertEquals("wrong SATE", state, TransportStateView.BOOKED);
    }
    
    //------------------------------------------------updateTransportState()
    /*
    @Test 
    public void sucessUpdatingTransport(@Mocked TransporterClient transporterClientMock) throws Exception{
    	
    	
    	
    	new Expectations() {{
            transporterClientMock.decideJob("1", true); result = new JobView();
        }};
        new Verifications() {{
            transporterClientMock.decideJob("1", true); maxTimes = 1;
        }};
    	
        
    	Date init = new Date();
		while (init.getTime() + 16000 > new Date().getTime());
		Transport t1 = manager.updateTransportState("1");
		
		assertEquals(t1.getState(), TransportStateView.COMPLETED);	
    
    }
    */
}
