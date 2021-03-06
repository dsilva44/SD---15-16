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
    private final String wsName = "UpaBroker";
    private final String ksPath = "src/main/resources/UpaBroker.jks";
    private final String password = "passUpaBroker";
    private final String uddiURL = "http://localhost:9090";
    private final String primaryURL = "http://localhost:9091/broker-ws/endpoint";
    private final String backupURL = "http://localhost:9092/broker-ws/endpoint";
    private final String transporterQuery = "UpaTransporter%";
    private final String wsURL1 = "http://localhost:8081/transporter-ws/endpoint";
    private final String wsURL2 = "http://localhost:8082/transporter-ws/endpoint";
    private Collection<String> endpointsList = new ArrayList<>(Arrays.asList(wsURL1, wsURL2));
    private ArrayList<String> emptyList = new ArrayList<>();

    private final String centroCity1 = "LisBOA";
    private final String centroCity2 = "LeIRia";

    private JobView offer1, offer2, offer3, offer4;
    private Transport transport;

    // initialization and clean-up for each test
    @Before
    public void setUp() {
        offer1 = new JobView(); offer1.setJobIdentifier("1"); offer1.setCompanyName("UpaTransporter1");
        offer2 = new JobView(); offer2.setJobIdentifier("2"); offer2.setCompanyName("UpaTransporter2");
        offer3 = new JobView(); offer3.setJobIdentifier("3"); offer3.setCompanyName("UpaTransporter3");
        offer4 = new JobView(); offer4.setJobIdentifier("4"); offer4.setCompanyName("UpaTransporter4");

        transport = new Transport();
        transport.setId("1");
        transport.setState(TransportStateView.REQUESTED);

        EndpointManager endpointManager = new EndpointManager(backupURL, wsName, uddiURL);
        Broker broker = new BrokerBackup(primaryURL);
        manager.init(endpointManager, broker, ksPath, password);
    }

    @After
    public void tearDown() {
        manager.getTransportsList().clear();
    }

    // --------------------------------------- updateTransporters ------------------------------------------------------
    @Test
    public void successUpdateTransportersList(@Mocked UDDINaming uddiNamingMock)
            throws JAXRException {
        new Expectations() {{
            uddiNamingMock.list(transporterQuery); result = endpointsList;
        }};

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

        boolean result = manager.updateTransportersList();

        new Verifications() {{
            uddiNamingMock.list(transporterQuery); maxTimes = 1;
        }};

        assertTrue("transporters list is not empty", manager.getTransporterClients().isEmpty());
        assertFalse("must return false", result);
    }
    // ----------------------------------------- findTransporters ------------------------------------------------------

    @Test
    public void successPingAllTransporters(@Mocked UDDINaming uddiNamingMock, @Mocked TransporterClient transporterClientMock)
            throws JAXRException {
        new Expectations() {{
            uddiNamingMock.list(transporterQuery); result = endpointsList;
            transporterClientMock.ping("0"); result = "Pong 0!";
            transporterClientMock.ping("1"); result = "Pong 1!";
        }};

        int result = manager.findTransporters();

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

        int result = manager.findTransporters();

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

        int result = manager.findTransporters();

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
		manager.addTransport(transport);

		assertEquals(transport.getId(), manager.getTransportById("1").getId());
	}

    //-------------------------------------requestTransport(origin, destination, price)---------------------------------
    @Test
    public void unknownOrigin()
            throws  Exception{
        String invalidDestination = "PoTATO";
        int price = 50;

        try {
            manager.requestTransport(invalidDestination, centroCity1, price);
            fail();
        } catch (UnknownLocationFault_Exception e) {
            assertTrue("added to list", manager.getTransportsList().isEmpty());
        }
    }

    @Test
    public void unknownDestination() throws  Exception {
        int price = 50;

        try {
            manager.requestTransport(centroCity2, null, price);
            fail();
        } catch (UnknownLocationFault_Exception e) {
            assertTrue("added to list", manager.getTransportsList().isEmpty());
        }
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
        offer1.setJobState(JobStateView.PROPOSED);

        new Expectations() {{
            uddiNamingMock.list(transporterQuery); result = endpointsList;
            transporterClientMock.requestJob(centroCity1, centroCity2, referencePrice);
            result = null;
            transporterClientMock.requestJob(centroCity1, centroCity2, referencePrice);
            result = offer1;
        }};

        Transport transport = manager.requestTransport(centroCity1, centroCity2, referencePrice);

        new Verifications() {{
            uddiNamingMock.list(transporterQuery); maxTimes = 1;
            transporterClientMock.requestJob(centroCity1, centroCity2, referencePrice); maxTimes = 2;
        }};

        assertEquals("state don't changed", transport.getState(), TransportStateView.BUDGETED);
        assertNotNull("transport id not set", transport.getId());
    }

    @Test
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
            fail();
        } catch (UnavailableTransportFault_Exception e) {

            assertTrue("transporter offer saved", manager.getTransportsList().isEmpty());

        }
    }

    //-------------------------------------------decideBestOffer()------------------------------------------------------
    @Test
    public void referencePrice0ShouldReturn0(@Mocked TransporterClient transporterClientMock) throws  Exception {
        transport.setPrice(0);
        offer1.setJobPrice(0); offer2.setJobPrice(0);
        transport.addOffer(offer1); transport.addOffer(offer2);

        new Expectations() {{
            transporterClientMock.decideJob("1", false); result = new JobView();
            transporterClientMock.decideJob("2", true); result = new JobView();
        }};

        manager.decideBestOffer(transport);

        new Verifications() {{
            transporterClientMock.decideJob("1", false); maxTimes = 1;
            transporterClientMock.decideJob("2", true); maxTimes = 1;
        }};

        assertTrue("not in list", manager.getTransportsList().contains(transport));
        assertEquals("wrong STATE", transport.getState(), TransportStateView.BOOKED);
        assertNotNull("transport company name not set", transport.getTransporterCompany());
        assertNotNull("chosen offer id is not set", transport.getChosenOfferID());
        assertEquals("not choose best offer", offer2.getJobIdentifier(), transport.getChosenOfferID());
        assertEquals("not correct price", 0,transport.getPrice());
    }

    @Test
    public void referencePrice1ShouldReturn0(@Mocked TransporterClient transporterClientMock) throws  Exception {
        transport.setPrice(1);
        offer1.setJobPrice(0); offer2.setJobPrice(0);
        transport.addOffer(offer1); transport.addOffer(offer2);

        new Expectations() {{
            transporterClientMock.decideJob("2", true); result = new JobView();
            transporterClientMock.decideJob("1", false); result = new JobView();

        }};

        manager.decideBestOffer(transport);

        new Verifications() {{
            transporterClientMock.decideJob("2", true); maxTimes = 1;
            transporterClientMock.decideJob("1", false); maxTimes = 1;
        }};

        assertTrue("not in list", manager.getTransportsList().contains(transport));
        assertEquals("wrong STATE", transport.getState(), TransportStateView.BOOKED);
        assertNotNull("transport company name not set", transport.getTransporterCompany());
        assertNotNull("chosen offer id is not set", transport.getChosenOfferID());
        assertEquals("not choose best offer", offer2.getJobIdentifier(), transport.getChosenOfferID());
        assertEquals("not correct price", 0,transport.getPrice());
    }

    @Test
    public void referencePriceBelowOrEqualTo10ShouldReturnPriceBelow10(@Mocked TransporterClient transporterClientMock) throws  Exception {
        transport.setPrice(10);
        offer1.setJobPrice(9); offer2.setJobPrice(5); offer3.setJobPrice(0); offer4.setJobPrice(1);
        transport.addOffer(offer1); transport.addOffer(offer2);
        transport.addOffer(offer3); transport.addOffer(offer4);

        new Expectations() {{
            transporterClientMock.decideJob("1", false); result = new JobView();
            transporterClientMock.decideJob("2", false); result = new JobView();
            transporterClientMock.decideJob("3", true); result = new JobView();
            transporterClientMock.decideJob("4", false); result = new JobView();

        }};

        manager.decideBestOffer(transport);

        new Verifications() {{
            transporterClientMock.decideJob("1", false);
            transporterClientMock.decideJob("2", false); maxTimes = 1;
            transporterClientMock.decideJob("3", true); maxTimes = 1;
            transporterClientMock.decideJob("4", false); maxTimes = 1;
        }};

        assertTrue("not in list", manager.getTransportsList().contains(transport));
        assertEquals("wrong STATE", transport.getState(), TransportStateView.BOOKED);
        assertNotNull("transport company name not set", transport.getTransporterCompany());
        assertNotNull("chosen offer id is not set", transport.getChosenOfferID());
        assertEquals("not choose best offer", offer3.getJobIdentifier(), transport.getChosenOfferID());
        assertEquals("not correct price", 0,transport.getPrice());
    }

    @Test
    public void someOffersBelowReferencePrice(
            @Mocked TransporterClient transporterClientMock) throws  Exception {
        transport.setPrice(15);
        offer1.setJobPrice(9); offer2.setJobPrice(5); offer3.setJobPrice(15); offer4.setJobPrice(6);
        transport.addOffer(offer1); transport.addOffer(offer2);
        transport.addOffer(offer3); transport.addOffer(offer4);

        new Expectations() {{
            transporterClientMock.decideJob("1", false); result = new JobView();
            transporterClientMock.decideJob("2", true); result = new JobView();
            transporterClientMock.decideJob("3", false); result = new JobView();
            transporterClientMock.decideJob("4", false); result = new JobView();
        }};

        manager.decideBestOffer(transport);

        new Verifications() {{
            transporterClientMock.decideJob("1", false); maxTimes = 1;
            transporterClientMock.decideJob("2", true); maxTimes = 1;
            transporterClientMock.decideJob("3", false); maxTimes = 1;
            transporterClientMock.decideJob("4", false); maxTimes = 1;
        }};

        assertTrue("not in list", manager.getTransportsList().contains(transport));
        assertEquals("wrong STATE", transport.getState(), TransportStateView.BOOKED);
        assertNotNull("transport company name not set", transport.getTransporterCompany());
        assertNotNull("chosen offer id is not set", transport.getChosenOfferID());
        assertEquals("not choose best offer", offer2.getJobIdentifier(), transport.getChosenOfferID());
        assertEquals("not correct price", 5,transport.getPrice());
    }

    @Test(expected = UnavailableTransportPriceFault_Exception.class)
    public void allOffersAboveReferencePrice(
            @Mocked TransporterClient transporterClientMock) throws  Exception {
        transport.setPrice(15);
        offer1.setJobPrice(15); offer2.setJobPrice(20); offer3.setJobPrice(16); offer4.setJobPrice(100);
        transport.addOffer(offer1); transport.addOffer(offer2);
        transport.addOffer(offer3); transport.addOffer(offer4);

        new Expectations() {{
            transporterClientMock.decideJob("1", false); result = new JobView();
            transporterClientMock.decideJob("2", false); result = new JobView();
            transporterClientMock.decideJob("3", false); result = new JobView();
            transporterClientMock.decideJob("4", false); result = new JobView();
        }};

        manager.decideBestOffer(transport);

        new Verifications() {{
            transporterClientMock.decideJob("1", false); maxTimes = 1;
            transporterClientMock.decideJob("2", false); maxTimes = 1;
            transporterClientMock.decideJob("3", false); maxTimes = 1;
            transporterClientMock.decideJob("4", false); maxTimes = 1;
        }};

        try {
            manager.decideBestOffer(transport);

        } catch (UnavailableTransportPriceFault_Exception e) {
            assertEquals("wrong SATE", transport.getState(), TransportStateView.FAILED);
            assertNotNull("transport company name not set", transport.getTransporterCompany());
            assertNotNull("chosen offer id is not set", transport.getChosenOfferID());

            throw new UnavailableTransportPriceFault_Exception(null, null);
        }
    }

    @Test
    public void offersSamePriceBelowReferencePrice(
            @Mocked TransporterClient transporterClientMock) throws  Exception {
        transport.setPrice(15);
        offer1.setJobPrice(14); offer2.setJobPrice(14); offer3.setJobPrice(14); offer4.setJobPrice(14);
        transport.addOffer(offer1); transport.addOffer(offer2);
        transport.addOffer(offer3); transport.addOffer(offer4);

        new Expectations() {{
            transporterClientMock.decideJob("1", true); result = new JobView();
            transporterClientMock.decideJob("2", false); result = new JobView();
            transporterClientMock.decideJob("3", false); result = new JobView();
            transporterClientMock.decideJob("4", false); result = new JobView();
        }};

        manager.decideBestOffer(transport);

        new Verifications() {{
            transporterClientMock.decideJob("1", true); maxTimes = 1;
            transporterClientMock.decideJob("2", false); maxTimes = 1;
            transporterClientMock.decideJob("3", false); maxTimes = 1;
            transporterClientMock.decideJob("4", false); maxTimes = 1;
        }};

        assertTrue("not in list", manager.getTransportsList().contains(transport));
        assertEquals("wrong SATE", transport.getState(), TransportStateView.BOOKED);
        assertNotNull("transport company name not set", transport.getTransporterCompany());
        assertNotNull("chosen offer id is not set", transport.getChosenOfferID());
        assertEquals("not choose best offer", offer1.getJobIdentifier(), transport.getChosenOfferID());
        assertEquals("not correct price", 14,transport.getPrice());
    }
    
    //-----------------------------------------updateTransportState(String id) -----------------------------------------
    @Test
    public void successUpdateRequestedTransport(@Mocked TransporterClient transporterClientMock) throws UnknownTransportFault_Exception {
        Transport t1 = new Transport(); t1.setId("1"); t1.setState(TransportStateView.REQUESTED);
        JobView jobView = new JobView(); jobView.setJobState(JobStateView.PROPOSED);
        manager.addTransport(t1);
        t1.setChosenOfferID("1");

        new Expectations() {{
            transporterClientMock.jobStatus("1"); result = jobView;
        }};

        manager.updateTransportState("1");

        new Verifications() {{
            transporterClientMock.jobStatus("1"); maxTimes = 1;
        }};

        assertEquals("not update transport status", TransportStateView.BUDGETED, t1.getState());
    }

    @Test(expected = UnknownTransportFault_Exception.class)
    public void returnWrongJobState(@Mocked TransporterClient transporterClientMock) throws UnknownTransportFault_Exception {
        manager.updateTransportState("1");
    }

    @Test
    public void successUpdateBudgetTransport(@Mocked TransporterClient transporterClientMock) throws UnknownTransportFault_Exception {
        Transport t1 = new Transport(); t1.setId("1"); t1.setState(TransportStateView.BUDGETED);
        JobView jobView = new JobView(); jobView.setJobState(JobStateView.ACCEPTED);
        t1.setChosenOfferID("1");
        manager.addTransport(t1);

        new Expectations() {{
            transporterClientMock.jobStatus("1"); result = jobView;
        }};

        manager.updateTransportState("1");

        new Verifications() {{
            transporterClientMock.jobStatus("1"); maxTimes = 1;
        }};

        assertEquals("not update transport status", TransportStateView.BOOKED, t1.getState());
    }

    @Test
    public void successUpdateRejectedBudgetTransport(@Mocked TransporterClient transporterClientMock) throws UnknownTransportFault_Exception {
        Transport t1 = new Transport(); t1.setId("1"); t1.setState(TransportStateView.BUDGETED);
        JobView jobView = new JobView(); jobView.setJobState(JobStateView.REJECTED);
        t1.setChosenOfferID("1");
        manager.addTransport(t1);

        new Expectations() {{
            transporterClientMock.jobStatus("1"); result = jobView;
        }};

        manager.updateTransportState("1");

        new Verifications() {{
            transporterClientMock.jobStatus("1"); maxTimes = 1;
        }};

        assertEquals("not update transport status", TransportStateView.FAILED, t1.getState());
    }

    @Test
    public void successUpdateBookedTransport(@Mocked TransporterClient transporterClientMock) throws UnknownTransportFault_Exception {
        Transport t1 = new Transport(); t1.setId("1"); t1.setState(TransportStateView.BOOKED);
        JobView jobView = new JobView(); jobView.setJobState(JobStateView.HEADING);
        t1.setChosenOfferID("1");
        manager.addTransport(t1);

        new Expectations() {{
            transporterClientMock.jobStatus("1"); result = jobView;
        }};

        manager.updateTransportState("1");

        new Verifications() {{
            transporterClientMock.jobStatus("1"); maxTimes = 1;
        }};

        assertEquals("not update transport status", TransportStateView.HEADING, t1.getState());
    }

    @Test
    public void successUpdateBookedToCompletedTransport(@Mocked TransporterClient transporterClientMock) throws UnknownTransportFault_Exception {
        Transport t1 = new Transport();t1.setId("1"); t1.setState(TransportStateView.BOOKED);
        JobView jobView = new JobView(); jobView.setJobState(JobStateView.COMPLETED);
        t1.setChosenOfferID("1");
        manager.addTransport(t1);

        new Expectations() {{
            transporterClientMock.jobStatus("1"); result = jobView;
        }};

        manager.updateTransportState("1");

        new Verifications() {{
            transporterClientMock.jobStatus("1"); maxTimes = 1;
        }};

        assertEquals("not update transport status", TransportStateView.COMPLETED, t1.getState());
    }

    @Test
    public void successUpdateCompletedTransport(@Mocked TransporterClient transporterClientMock) throws UnknownTransportFault_Exception {
        Transport t1 = new Transport(); t1.setId("1"); t1.setState(TransportStateView.COMPLETED);
        JobView jobView = new JobView(); jobView.setJobState(JobStateView.COMPLETED);
        t1.setChosenOfferID("1");
        manager.addTransport(t1);

        new Expectations() {{
            transporterClientMock.jobStatus("1"); result = jobView;
        }};

        manager.updateTransportState("1");

        new Verifications() {{
            transporterClientMock.jobStatus("1"); maxTimes = 1;
        }};

        assertEquals("not update transport status", TransportStateView.COMPLETED, t1.getState());
    }

    @Test
    public void successUpdateFailedTransport(@Mocked TransporterClient transporterClientMock) throws UnknownTransportFault_Exception {
        Transport t1 = new Transport(); t1.setId("1"); t1.setState(TransportStateView.FAILED);
        JobView jobView = new JobView(); jobView.setJobState(JobStateView.COMPLETED);
        t1.setChosenOfferID("1");
        manager.addTransport(t1);

        new Expectations() {{
            transporterClientMock.jobStatus("1"); result = jobView;
        }};

        manager.updateTransportState("1");

        new Verifications() {{
            transporterClientMock.jobStatus("1"); maxTimes = 1;
        }};

        assertEquals("not update transport status", TransportStateView.COMPLETED, t1.getState());
    }

    //-----------------------------------------updateTransport(...) -----------------------------------------
    @Test
    public void successUpdateNotExistentTransport() {
        manager.updateTransport(transport.toTransportView(), "UpaTransporter_1", "tID_1", "oprID_1");

        assertNotNull("Transport not added", manager.getTransportById(transport.getId()));
        assertEquals("Res not added", manager.getTransportResponses().get("oprID_1"), "tID_1");
    }

    @Test
    public void successUpdateExistentTransport() {
        manager.addTransport(transport);
        manager.getTransportResponses().put("oprID_1", "tID_1");

        assertEquals(TransportStateView.REQUESTED, manager.getTransportById("1").getState());

        Transport newT = new Transport(); newT.setId("1"); newT.setState(TransportStateView.COMPLETED);
        manager.updateTransport(newT.toTransportView(), "UpaTransporter_1", "oprID_2" ,"tID_1");

        assertEquals("Not update",TransportStateView.COMPLETED, manager.getTransportById("1").getState());
        assertEquals("Chosen offer not update", "UpaTransporter_1", manager.getTransportById("1").getChosenOfferID());
        assertEquals("Res not added", manager.getTransportResponses().get("oprID_2"), "tID_1");
    }
}
