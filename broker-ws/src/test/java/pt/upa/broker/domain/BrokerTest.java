package pt.upa.broker.domain;

import mockit.Expectations;
import mockit.Mocked;
import org.junit.Before;
import org.junit.Test;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.exception.BrokerUddiNamingException;
import pt.upa.broker.ws.EndpointManager;

import javax.xml.registry.JAXRException;

import static org.junit.Assert.*;

public class BrokerTest {
    private EndpointManager endpointManager;
    private final String wsName = "UpaBroker";
    private final String validUddiURL = "http://localhost:9090";
    private final String wsURL1 = "http://localhost:8081/broker-ws/endpoint";
    private final String wsURL2 = "http://localhost:8082/broker-ws/endpoint";

    @Before
    public void setUp() {
        endpointManager = new EndpointManager(wsURL1, wsURL2, wsName);
    }

    @Test
    public void successInitialization() {
        Broker broker = new BrokerPrimary(validUddiURL, endpointManager);

        assertNotNull("endpointManager not initialize correctly", broker.getEndPointManager());
        assertNotNull("uddiURL not initialize correctly", broker.getUddiURL());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidUddiURL() {
        String invalidUddiURL = "POTATO";
        new BrokerPrimary(invalidUddiURL, endpointManager);
    }

    @Test
    public void successRegisterAndDeleteFromUddi(@Mocked UDDINaming uddiNamingMock) throws Exception {
        Broker broker = new BrokerPrimary(validUddiURL, endpointManager);

        new Expectations() {{
            uddiNamingMock.rebind(endpointManager.getWsName(), endpointManager.getWsURL1());
            uddiNamingMock.unbind(endpointManager.getWsName());
        }};

        broker.registerUddi();
        assertTrue("register status not changed", broker.isRegister());

        broker.deleteFromUDDI();
        assertFalse("register status not changed", broker.isRegister());
    }

    @Test
    public void successDeleteWhenNotRegister() throws Exception {
        Broker broker = new BrokerPrimary(validUddiURL, endpointManager);

        broker.deleteFromUDDI();
        assertFalse("register status not changed", broker.isRegister());
    }

    @Test (expected = BrokerUddiNamingException.class)
    public void stopShouldTrowExceptionWhenErrorOccurredOnUddiNaming (@Mocked UDDINaming uddiNamingMock) throws JAXRException {
        Broker broker = new BrokerPrimary(validUddiURL, endpointManager);

        new Expectations() {{
            uddiNamingMock.rebind(endpointManager.getWsName(), endpointManager.getWsURL1());
            uddiNamingMock.unbind(endpointManager.getWsName()); result = new Exception();
        }};

        broker.registerUddi();
        assertTrue("register status not changed", broker.isRegister());

        broker.deleteFromUDDI();
    }

}
