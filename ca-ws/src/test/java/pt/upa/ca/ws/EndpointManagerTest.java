package pt.upa.ca.ws;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.*;
import org.junit.runner.RunWith;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.ca.exception.CAEndpointException;
import pt.upa.ca.exception.CAUddiNamingException;

import javax.xml.registry.JAXRException;
import javax.xml.ws.Endpoint;

import static org.junit.Assert.*;

/**
 *  Unit Test EndpointManager
 *
 *  Invoked by Maven in the "test" life-cycle phase
 *  If necessary, should invoke "mock" remote servers
 */
@SuppressWarnings("Duplicates")
@RunWith(JMockit.class)
public class EndpointManagerTest {
    // static members

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() {
    }

    @AfterClass
    public static void oneTimeTearDown() {

    }

    // members
    private final String CA_NAME = "UpaCA";
    private final String UDDI_URL = "http://localhost:9090";
    private final String WS_URL = "http://localhost:8080/broker-ws/endpoint";

    // initialization and clean-up for each test
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {

    }


    // tests
    @Test
    public void successInitialization() {
        EndpointManager endpointManager = new EndpointManager(UDDI_URL, WS_URL);
        assertEquals("uddiURL not properly set", UDDI_URL, endpointManager.getUddiURL());
        assertEquals("wsURL not properly set", WS_URL, endpointManager.getWsURL());

        assertNotNull("endpoint not initialize correctly", endpointManager.getEndpoint());
        assertNotNull("uddiNaming not initialize correctly", endpointManager.getUddiURL());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidUddiURL() {
        String invalidUddiURL = "POTATO";
        new EndpointManager(invalidUddiURL, WS_URL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidWsURL() {
        String invalidWsURL = "POTATO";
        new EndpointManager(UDDI_URL, invalidWsURL);
    }

    @Test
    public void successStart(@Mocked Endpoint endpointMock) {
        EndpointManager endpointManager = new EndpointManager(UDDI_URL, WS_URL);

        UDDINaming  uddiNamingMock = new MockUp<UDDINaming>() {
            @Mock
            void rebind(String wsName, String wsURL) {
                assertEquals("wsName not pass correctly", wsName, endpointManager.getWsName());
                assertEquals("wsURL not pass correctly", wsURL, endpointManager.getWsURL());
            }
        }.getMockInstance();

        endpointManager.setUddiNaming(uddiNamingMock);
        endpointManager.setEndpoint(endpointMock);

        endpointManager.start();

        assertTrue("started status is not set to true", endpointManager.isStarted());
    }

    @Test(expected = CAEndpointException.class)
    public void startShouldTrowExceptionWhenErrorOccurredOnEndpoint (
            @Mocked Endpoint endpointMock, @Mocked UDDINaming uddiNamingMock) throws JAXRException {
        EndpointManager endpointManager = new EndpointManager(UDDI_URL, WS_URL);

        new Expectations() {{
            endpointMock.publish(WS_URL); result = new Exception();
        }};

        endpointManager.setAwaitConnection(true);
        endpointManager.setStarted(true);
        endpointManager.setUddiNaming(uddiNamingMock);
        endpointManager.setEndpoint(endpointMock);

        endpointManager.start();
    }

    @Test(expected = CAUddiNamingException.class)
    public void startShouldTrowExceptionWhenErrorOccurredOnUddiNaming (
            @Mocked Endpoint endpointMock, @Mocked UDDINaming uddiNamingMock) throws JAXRException {
        EndpointManager endpointManager = new EndpointManager(UDDI_URL, WS_URL);

        new Expectations() {{
            uddiNamingMock.rebind(CA_NAME, WS_URL); result = new Exception();
        }};

        endpointManager.setAwaitConnection(true);
        endpointManager.setStarted(true);
        endpointManager.setUddiNaming(uddiNamingMock);
        endpointManager.setEndpoint(endpointMock);

        endpointManager.start();
    }


    @Test
    public void successAwaitConnection() {
        EndpointManager endpointManager = new EndpointManager(UDDI_URL, WS_URL);

        endpointManager.setStarted(true);

        boolean awaitConnectionReturn = endpointManager.awaitConnections();

        assertTrue("fail to set await status", endpointManager.isAwaitConnection());
        assertTrue("must return true", awaitConnectionReturn );
    }

    @Test
    public void shouldNotAwaitConnectionsWhenWsIsNotStarted() {
        EndpointManager endpointManager = new EndpointManager(UDDI_URL, WS_URL);

        endpointManager.setStarted(false);

        boolean awaitConnectionReturn = endpointManager.awaitConnections();

        assertFalse("fail to set await status", endpointManager.isAwaitConnection());
        assertFalse("must return false", awaitConnectionReturn );
    }

    @Test
    public void successStopWhenWsIsStarted(@Mocked Endpoint endpointMock) {
        EndpointManager endpointManager = new EndpointManager(UDDI_URL, WS_URL);


        UDDINaming  uddiNamingMock = new MockUp<UDDINaming>() {
            @Mock
            void unbind(String wsName) {
                assertEquals("wsName not pass correctly", wsName, endpointManager.getWsName());
            }
        }.getMockInstance();
        endpointManager.setStarted(true);
        endpointManager.setAwaitConnection(false);
        endpointManager.setEndpoint(endpointMock);
        endpointManager.setUddiNaming(uddiNamingMock);

        endpointManager.stop();

        assertFalse("started status not changed", endpointManager.isStarted());
        assertFalse("await status not changed", endpointManager.isAwaitConnection());
    }

    @Test
    public void successStopWhenWsIsAwaiting(@Mocked Endpoint endpointMock) throws Exception {
        EndpointManager endpointManager = new EndpointManager(UDDI_URL, WS_URL);

        UDDINaming  uddiNamingMock = new MockUp<UDDINaming>() {
            @Mock
            void unbind(String wsName) {
                assertEquals("wsName not pass correctly", wsName, endpointManager.getWsName());
            }
        }.getMockInstance();
        endpointManager.setAwaitConnection(true);
        endpointManager.setStarted(false);
        endpointManager.setEndpoint(endpointMock);
        endpointManager.setUddiNaming(uddiNamingMock);

        endpointManager.stop();

        assertFalse("started status not changed", endpointManager.isStarted());
        assertFalse("await status not changed", endpointManager.isAwaitConnection());
    }

    @Test
    public void successStopWhenEndpointAndUddiNamingIsNull() throws Exception {
        EndpointManager endpointManager = new EndpointManager(UDDI_URL, WS_URL);

        endpointManager.setEndpoint(null);
        endpointManager.setUddiNaming(null);

        endpointManager.stop();

        assertFalse("started status not changed", endpointManager.isStarted());
        assertFalse("await status not changed", endpointManager.isAwaitConnection());
    }

    @Test
    public void successStopWhenWsIsNotStartedAndAwaitingConnections(
            @Mocked Endpoint endpointMock, @Mocked UDDINaming uddiNamingMock) throws Exception {
        EndpointManager endpointManager = new EndpointManager(UDDI_URL, WS_URL);

        endpointManager.setEndpoint(endpointMock);
        endpointManager.setUddiNaming(uddiNamingMock);
        endpointManager.setAwaitConnection(false);
        endpointManager.setStarted(false);

        endpointManager.stop();

        assertFalse("started status not changed", endpointManager.isStarted());
        assertFalse("await status not changed", endpointManager.isAwaitConnection());
    }

    @Test (expected = CAEndpointException.class)
    public void stopShouldTrowExceptionWhenErrorOccurredOnEndpoint (
            @Mocked Endpoint endpointMock, @Mocked UDDINaming uddiNamingMock) throws JAXRException {
        EndpointManager endpointManager = new EndpointManager(UDDI_URL, WS_URL);

        new Expectations() {{
            endpointMock.stop(); result = new Exception();
        }};

        endpointManager.setAwaitConnection(true);
        endpointManager.setStarted(true);
        endpointManager.setUddiNaming(uddiNamingMock);
        endpointManager.setEndpoint(endpointMock);

        endpointManager.stop();
    }

    @Test (expected = CAUddiNamingException.class)
    public void stopShouldTrowExceptionWhenErrorOccurredOnUddiNaming (
            @Mocked Endpoint endpointMock, @Mocked UDDINaming uddiNamingMock) throws JAXRException {
        EndpointManager endpointManager = new EndpointManager(UDDI_URL, WS_URL);

        new Expectations() {{
            uddiNamingMock.unbind(CA_NAME); result = new Exception();
        }};

        endpointManager.setAwaitConnection(true);
        endpointManager.setStarted(true);
        endpointManager.setUddiNaming(uddiNamingMock);
        endpointManager.setEndpoint(endpointMock);

        endpointManager.stop();
    }
}
