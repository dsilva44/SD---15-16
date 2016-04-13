package pt.upa.broker.domain;

import mockit.*;
import org.junit.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.Exception.CannotUpdateTransportersClientsException;
import pt.upa.broker.domain.Transport;
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
    private String transporterQuery = "UpaTransporter%";
    private String wsURL1 = "http://localhost:8081/transporter-ws/endpoint";
    private String wsURL2 = "http://localhost:8082/transporter-ws/endpoint";
    private Collection<String> transEndpoints = new ArrayList<>(Arrays.asList(wsURL1, wsURL2));
    private ArrayList<String> emptyList = new ArrayList<>();

    // initialization and clean-up for each test
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // --------------------------------------- updateTransporters ------------------------------------------------------
    @Test
    public void successUpdateTransportersList(@Mocked UDDINaming uddiNamingMock, @Mocked TransporterClient transporterClientMock)
            throws JAXRException {
        new Expectations() {{
            uddiNamingMock.list(transporterQuery); result = transEndpoints;
        }};
        manager.setUddiNaming(uddiNamingMock);

        boolean result = manager.updateTransportersList();

        new Verifications() {{
            uddiNamingMock.list(transporterQuery); maxTimes = 1;
        }};

        assertFalse("transporters list is empty", manager.getTransporterClients().isEmpty());
        assertTrue("must return true", result);
    }

    @Test(expected = CannotUpdateTransportersClientsException.class)
    public void shouldTrowExceptionWhenSomethingGoesWrong(@Mocked UDDINaming uddiNamingMock) throws JAXRException {
        new Expectations() {{
            uddiNamingMock.list(transporterQuery); result = new JAXRException();
        }};
        manager.setUddiNaming(uddiNamingMock);

        manager.updateTransportersList();
    }

    @Test
    public void cannotFindTransportersShouldReturnFalse(@Mocked UDDINaming uddiNamingMock) throws JAXRException {
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
            uddiNamingMock.list(transporterQuery); result = transEndpoints;
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
    public void successPingSomeTransporters(@Mocked UDDINaming uddiNamingMock, @Mocked TransporterClient transporterClientMock)
            throws JAXRException {
        new Expectations() {{
            uddiNamingMock.list(transporterQuery); result = transEndpoints;
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
    }
    
    //--------------------------------------------------------------getTransportByID-------------
    
    @Test
	public void successGetTransportNonExisting(){
		assertNull(manager.getTransportById("invalidID"));
	}
    
    @Test
	public void successGetTransportExisting() {
		Transport t1 = new Transport();
		t1.setId("id1");
		manager.addTransport(t1);

		assertEquals(t1, manager.getTransportById("id1"));
	}

	
}
