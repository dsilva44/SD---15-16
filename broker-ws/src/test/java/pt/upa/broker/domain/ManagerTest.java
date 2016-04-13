package pt.upa.broker.domain;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.Exception.BrokerUddiNamingException;

import javax.xml.registry.JAXRException;

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
    private String wsName = "UpaTransporter1";
    private String wsURL1 = "http://localhost:8081/transporter-ws/endpoint";
    private String wsURL2 = "http://localhost:8082/transporter-ws/endpoint";

    // initialization and clean-up for each test
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // --------------------------------------- updateTransporters ------------------------------------------------------
    @Test
    public void successUpdateTransportersList(@Mocked UDDINaming uddiNamingMock) throws JAXRException {
        new Expectations() {{
            uddiNamingMock.lookup(wsName); result = wsURL1; result = wsURL2;
        }};
        manager.setUddiNaming(uddiNamingMock);

        boolean result = manager.updateTransportersList();

        new Verifications() {{
            uddiNamingMock.lookup(wsName); maxTimes = 2;
        }};

        assertFalse("transporters list is empty", manager.getAvailTransporters().isEmpty());
        assertTrue("must return true", result);
    }

    @Test(expected = BrokerUddiNamingException.class)
    public void shouldTrowExceptionWhenJuddIsDown(@Mocked UDDINaming uddiNamingMock) throws JAXRException {
        new Expectations() {{
            uddiNamingMock.lookup("BATATA"); result = new JAXRException();
        }};
        manager.setUddiNaming(uddiNamingMock);

        manager.updateTransportersList();
    }

    @Test
    public void cannotFindTransportersShouldReturnFalse(@Mocked UDDINaming uddiNamingMock) throws JAXRException {
        new Expectations() {{
            uddiNamingMock.lookup(wsName); result = null;
        }};
        manager.setUddiNaming(uddiNamingMock);

        boolean result = manager.updateTransportersList();

        new Verifications() {{
            uddiNamingMock.lookup(wsName); maxTimes = 1;
        }};

        assertTrue("transporters list is not empty", manager.getAvailTransporters().isEmpty());
        assertTrue("must return false", result);
    }
    // ----------------------------------------- pingTransporters ------------------------------------------------------

    @Test
    public void successPingAllTransporters(@Mocked UDDINaming uddiNamingMock) throws JAXRException {
        new Expectations() {{
            uddiNamingMock.lookup("BATATA"); result = wsURL1; result = wsURL2;
        }};
        manager.setUddiNaming(uddiNamingMock);

        int result = manager.pingTransporters();

        new Verifications() {{
            uddiNamingMock.lookup(wsName); maxTimes = 2;
        }};

        assertTrue("must return 2", result == 2);
    }

    @Test
    public void failToPingTransporters(@Mocked UDDINaming uddiNamingMock) throws JAXRException {
        new Expectations() {{
            uddiNamingMock.lookup("BATATA"); result = null;
        }};
        manager.setUddiNaming(uddiNamingMock);

        int result = manager.pingTransporters();

        new Verifications() {{
            uddiNamingMock.lookup(wsName); maxTimes = 1;
        }};

        assertTrue("must return 0", result == 0);
    }
}
