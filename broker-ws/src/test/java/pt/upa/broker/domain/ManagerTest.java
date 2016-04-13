package pt.upa.broker.domain;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.Exception.BrokerUddiNamingException;

import javax.xml.registry.JAXRException;

import java.lang.reflect.Array;
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
    private String wsName = "UpaTransporter%";
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
    public void successUpdateTransportersList(@Mocked UDDINaming uddiNamingMock) throws JAXRException {
        new Expectations() {{
            uddiNamingMock.list(wsName); result = transEndpoints;
        }};
        manager.setUddiNaming(uddiNamingMock);

        boolean result = manager.updateTransportersList();

        new Verifications() {{
            uddiNamingMock.list(wsName); maxTimes = 1;
        }};

        assertFalse("transporters list is empty", manager.getAvailTransporters().isEmpty());
        assertTrue("must return true", result);
    }

    @Test(expected = BrokerUddiNamingException.class)
    public void shouldTrowExceptionWhenJuddIsDown(@Mocked UDDINaming uddiNamingMock) throws JAXRException {
        new Expectations() {{
            uddiNamingMock.list(wsName); result = new JAXRException();
        }};
        manager.setUddiNaming(uddiNamingMock);

        manager.updateTransportersList();
    }

    @Test
    public void cannotFindTransportersShouldReturnFalse(@Mocked UDDINaming uddiNamingMock) throws JAXRException {
        new Expectations() {{
            uddiNamingMock.list(wsName); result = emptyList;
        }};
        manager.setUddiNaming(uddiNamingMock);

        boolean result = manager.updateTransportersList();

        new Verifications() {{
            uddiNamingMock.list(wsName); maxTimes = 1;
        }};

        assertTrue("transporters list is not empty", manager.getAvailTransporters().isEmpty());
        assertTrue("must return false", result);
    }
    // ----------------------------------------- pingTransporters ------------------------------------------------------

    @Test
    public void successPingAllTransporters(@Mocked UDDINaming uddiNamingMock) throws JAXRException {
        new Expectations() {{
            uddiNamingMock.list(wsName); result = transEndpoints;
        }};
        manager.setUddiNaming(uddiNamingMock);

        int result = manager.pingTransporters();

        new Verifications() {{
            uddiNamingMock.list(wsName); maxTimes = 2;
        }};

        assertTrue("must return 2", result == 2);
    }

    @Test
    public void failToPingTransporters(@Mocked UDDINaming uddiNamingMock) throws JAXRException {
        new Expectations() {{
            uddiNamingMock.list(wsName); result = emptyList;
        }};
        manager.setUddiNaming(uddiNamingMock);

        int result = manager.pingTransporters();

        new Verifications() {{
            uddiNamingMock.list(wsName); maxTimes = 1;
        }};

        assertTrue("must return 0", result == 0);
    }
}
