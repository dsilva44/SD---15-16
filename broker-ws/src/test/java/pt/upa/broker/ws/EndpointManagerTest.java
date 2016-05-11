package pt.upa.broker.ws;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.*;
import org.junit.runner.RunWith;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.exception.BrokerEndpointException;
import pt.upa.broker.exception.BrokerUddiNamingException;

import javax.xml.registry.JAXRException;
import javax.xml.ws.Endpoint;

import static org.junit.Assert.*;

/**
 *  Unit Test EndpointManager
 *
 *  Invoked by Maven in the "test" life-cycle phase
 *  If necessary, should invoke "mock" remote servers
 */
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
    private String wsName = "UpaBroker";
    private String validWsURL = "http://localhost:8080/broker-ws/endpoint";
    private final String validUddiURL = "http://localhost:9090";

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
        EndpointManager endpointManager = new EndpointManager(validWsURL, wsName, validUddiURL);
        assertEquals("wsURL not properly set", validWsURL, endpointManager.getWsURL());

        assertNotNull("endpoint not initialize correctly", endpointManager.getEndpoint());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidWsURL() {
        String invalidWsURL = "POTATO";
        new EndpointManager(invalidWsURL, wsName, validUddiURL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidUddiURL() {
        String invalidUddiURL = "POTATO";
        new EndpointManager(validWsURL, wsName, invalidUddiURL);
    }

    @Test
    public void successStart(@Mocked Endpoint endpointMock) {
        EndpointManager endpointManager = new EndpointManager(validWsURL, wsName, validUddiURL);
        endpointManager.setEndpoint(endpointMock);

        endpointManager.start();

        assertTrue("started status is not set to true", endpointManager.isPublish());
    }

    @Test(expected = BrokerEndpointException.class)
    public void startShouldTrowExceptionWhenErrorOccurredOnEndpoint (
            @Mocked Endpoint endpointMock) throws JAXRException {
        EndpointManager endpointManager = new EndpointManager(validWsURL, wsName, validUddiURL);

        new Expectations() {{
            endpointMock.publish(validWsURL); result = new Exception();
        }};

        endpointManager.setAwaitConnection(true);
        endpointManager.setPublished(true);
        endpointManager.setEndpoint(endpointMock);

        endpointManager.start();
    }


    @Test
    public void successAwaitConnection() {
        EndpointManager endpointManager = new EndpointManager(validWsURL, wsName, validUddiURL);

        endpointManager.setPublished(true);

        boolean awaitConnectionReturn = endpointManager.awaitConnections();

        assertTrue("fail to set await status", endpointManager.isAwaitConnection());
        assertTrue("must return true", awaitConnectionReturn );
    }

    @Test
    public void shouldNotAwaitConnectionsWhenWsIsNotStarted() {
        EndpointManager endpointManager = new EndpointManager(validWsURL, wsName, validUddiURL);

        endpointManager.setPublished(false);

        boolean awaitConnectionReturn = endpointManager.awaitConnections();

        assertFalse("fail to set await status", endpointManager.isAwaitConnection());
        assertFalse("must return false", awaitConnectionReturn );
    }

    @Test
    public void successStopWhenWsIsStarted(@Mocked Endpoint endpointMock) {
        EndpointManager endpointManager = new EndpointManager(validWsURL, wsName, validUddiURL);

        endpointManager.setPublished(true);
        endpointManager.setAwaitConnection(false);
        endpointManager.setEndpoint(endpointMock);

        endpointManager.stop();

        assertFalse("started status not changed", endpointManager.isPublish());
        assertFalse("await status not changed", endpointManager.isAwaitConnection());
    }

    @Test
    public void successStopWhenWsIsAwaiting(@Mocked Endpoint endpointMock) throws Exception {
        EndpointManager endpointManager = new EndpointManager(validWsURL, wsName, validUddiURL);

        endpointManager.setAwaitConnection(true);
        endpointManager.setPublished(false);
        endpointManager.setEndpoint(endpointMock);

        endpointManager.stop();

        assertFalse("started status not changed", endpointManager.isPublish());
        assertFalse("await status not changed", endpointManager.isAwaitConnection());
    }

    @Test
    public void successStopWhenWsIsNotStartedAndAwaitingConnections(
            @Mocked Endpoint endpointMock, @Mocked UDDINaming uddiNamingMock) throws Exception {
        EndpointManager endpointManager = new EndpointManager(validWsURL, wsName, validUddiURL);

        endpointManager.setEndpoint(endpointMock);
        endpointManager.setAwaitConnection(false);
        endpointManager.setPublished(false);

        endpointManager.stop();

        assertFalse("started status not changed", endpointManager.isPublish());
        assertFalse("await status not changed", endpointManager.isAwaitConnection());
    }

    @Test (expected = BrokerEndpointException.class)
    public void stopShouldTrowExceptionWhenErrorOccurredOnEndpoint (@Mocked Endpoint endpointMock) throws JAXRException {
        EndpointManager endpointManager = new EndpointManager(validWsURL, wsName, validUddiURL);

        new Expectations() {{
            endpointMock.stop(); result = new Exception();
            endpointMock.isPublished(); result = true;
        }};

        endpointManager.setAwaitConnection(true);
        endpointManager.setPublished(true);
        endpointManager.setEndpoint(endpointMock);

        endpointManager.stop();
    }

    @Test
    public void successRegisterAndDeleteFromUddi(@Mocked UDDINaming uddiNamingMock) throws Exception {
        EndpointManager endpointManager = new EndpointManager(validWsURL, wsName, validUddiURL);

        new Expectations() {{
            uddiNamingMock.rebind(endpointManager.getWsName(), endpointManager.getWsURL());
            uddiNamingMock.unbind(endpointManager.getWsName());
        }};

        endpointManager.registerUddi();
        assertTrue("register status not changed", endpointManager.isRegister());

        endpointManager.stop();

        assertFalse("register status not changed", endpointManager.isRegister());
    }

    @Test (expected = BrokerUddiNamingException.class)
    public void stopShouldTrowExceptionWhenErrorOccurredOnUddiNaming (@Mocked UDDINaming uddiNamingMock) throws JAXRException {
        EndpointManager endpointManager = new EndpointManager(validWsURL, wsName, validUddiURL);

        new Expectations() {{
            uddiNamingMock.rebind(endpointManager.getWsName(), endpointManager.getWsURL());
            uddiNamingMock.unbind(endpointManager.getWsName()); result = new Exception();
        }};

        endpointManager.registerUddi();
        assertTrue("register status not changed", endpointManager.isRegister());

        endpointManager.stop();
    }
}
