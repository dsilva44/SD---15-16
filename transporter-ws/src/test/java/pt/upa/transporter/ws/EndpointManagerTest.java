package pt.upa.transporter.ws;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.*;
import org.junit.runner.RunWith;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.exception.InvalidTransporterNameException;
import pt.upa.transporter.exception.InvalidURLException;

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
    private String validUddiURL = "http://localhost:9090";
    private String invalidUddiURL = "POTATO";
    private String validWsName = "UpaTransporter1";
    private String invalidWsName = "UpaTransporterPOTATO";
    private String validWsURL = "http://localhost:8081/transporter-ws/endpoint";
    private String invalidWsURL = "POTATO";

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
        EndpointManager endpointManager = new EndpointManager(validUddiURL, validWsName, validWsURL);
        assertEquals("uddiURL not properly set", validUddiURL, endpointManager.getUddiURL());
        assertEquals("wsName not properly set", validWsName, endpointManager.getWsName());
        assertEquals("wsURL not properly set", validWsURL, endpointManager.getWsURL());
    }

    @Test(expected = InvalidURLException.class)
    public void invalidUddiURL() {
        EndpointManager endpointManager = new EndpointManager(invalidUddiURL, validWsName, validWsURL);
    }

    @Test(expected = InvalidTransporterNameException.class)
    public void invalidWsName() {
        EndpointManager endpointManager = new EndpointManager(validUddiURL, invalidWsName, validWsURL);
    }

    @Test(expected = InvalidURLException.class)
    public void invalidWsURL() {
        EndpointManager endpointManager = new EndpointManager(validUddiURL, validWsName, invalidWsURL);
    }

    @Test
    public void successStart() throws Exception {
        EndpointManager endpointManager = new EndpointManager(validUddiURL, validWsName, validWsURL);

        Endpoint endpointMock = new MockUp<Endpoint>(){
            @Mock
            void publish(String wsURL) {
                assertEquals("wsURL not pass correctly", wsURL, endpointManager.getWsURL());
            }
        }.getMockInstance();

        UDDINaming  uddiNamingMock = new MockUp<UDDINaming>() {
            @Mock
            void rebind(String wsName, String wsURL) {
                assertEquals("wsName not pass correctly", wsName, endpointManager.getWsName());
                assertEquals("wsURL not pass correctly", wsURL, endpointManager.getWsURL());
            }
        }.getMockInstance();

        endpointManager.setEndpoint(endpointMock);
        endpointManager.setUddiNaming(uddiNamingMock);

        endpointManager.start();

        assertTrue("fail to set started status", endpointManager.isStarted());
        assertTrue("must set started status", endpointManager.isStarted());
    }


    @Test
    public void successAwaitConnection(@Mocked Endpoint endpointMock) {
        EndpointManager endpointManager = new EndpointManager(validUddiURL, validWsName, validWsURL);

        new Expectations() {{
            endpointMock.isPublished(); result = true;
        }};
        endpointManager.setStarted(true);

        new Verifications() {{
            endpointMock.isPublished(); maxTimes = 1;
        }};

        boolean awaitConnectionReturn = endpointManager.awaitConnections();

        assertTrue("fail to set await status", endpointManager.isAwaitConnection());
        assertTrue("must return true", awaitConnectionReturn );
    }

    @Test
    public void shouldNotAwaitConnectionsWhenWsIsNotStarted(@Mocked Endpoint endpointMock) {
        EndpointManager endpointManager = new EndpointManager(validUddiURL, validWsName, validWsURL);

        new Expectations() {{
            endpointMock.isPublished(); result = true;
        }};
        endpointManager.setStarted(false);

        new Verifications() {{
            endpointMock.isPublished(); maxTimes = 1;
        }};

        boolean awaitConnectionReturn = endpointManager.awaitConnections();

        assertFalse("wrong set of await connection status", endpointManager.isAwaitConnection());
        assertFalse("must return false", awaitConnectionReturn );
    }

    @Test
    public void shouldNotAwaitConnectionsWhenWsURLIsNotPublish(@Mocked Endpoint endpointMock) {
        EndpointManager endpointManager = new EndpointManager(validUddiURL, validWsName, validWsURL);

        new Expectations() {{
            endpointMock.isPublished(); result = false;
        }};

        new Verifications() {{
            endpointMock.isPublished(); maxTimes = 1;
        }};
        endpointManager.setStarted(true);

        boolean awaitConnectionReturn = endpointManager.awaitConnections();

        assertFalse("wrong set of await connection status", endpointManager.isAwaitConnection());
        assertFalse("must return false", awaitConnectionReturn );
    }

    @Test
    public void successStopWhenWsIsStarted(@Mocked Endpoint endpointMock) {
        EndpointManager endpointManager = new EndpointManager(validUddiURL, validWsName, validWsURL);


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

        boolean stopReturn = endpointManager.stop();

        assertFalse("started status not changed", endpointManager.isStarted());
        assertTrue("must return true", stopReturn);
    }

    @Test
    public void successStopWhenWsIsAwaiting(@Mocked Endpoint endpointMock) {
        EndpointManager endpointManager = new EndpointManager(validUddiURL, validWsName, validWsURL);

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

        boolean stopReturn = endpointManager.stop();

        assertFalse("started status not changed", endpointManager.isStarted());
        assertTrue("must return true", stopReturn);
    }

    @Test
    public void successStopWhenEndpointAndUddiNamingIsNull() {
        EndpointManager endpointManager = new EndpointManager(validUddiURL, validWsName, validWsURL);

        endpointManager.setEndpoint(null);
        endpointManager.setUddiNaming(null);

        boolean stopReturn = endpointManager.stop();

        assertFalse("started status not changed", endpointManager.isStarted());
        assertTrue("must return true", stopReturn);
    }

    @Test
    public void successStopWhenWsIsNotStartedAndAwaitingConnections(@Mocked Endpoint endpointMock, @Mocked UDDINaming uddiNamingMock) {
        EndpointManager endpointManager = new EndpointManager(validUddiURL, validWsName, validWsURL);

        endpointManager.setEndpoint(endpointMock);
        endpointManager.setUddiNaming(uddiNamingMock);
        endpointManager.setAwaitConnection(false);
        endpointManager.setStarted(false);

        boolean stopReturn = endpointManager.stop();

        assertFalse("started status not changed", endpointManager.isStarted());
        assertTrue("must return true", stopReturn);
    }
}