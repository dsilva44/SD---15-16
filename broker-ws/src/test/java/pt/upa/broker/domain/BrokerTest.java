package pt.upa.broker.domain;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import pt.upa.broker.ws.TransportStateView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BrokerTest {

    private Manager manager = Manager.getInstance();
    private Transport transport;
    private final String primaryURL = "http://localhost:9091/broker-ws/endpoint";

    @Before
    public void setUp() {
        transport = new Transport();
        transport.setId("1");
        transport.setState(TransportStateView.REQUESTED);
    }

    //-----------------------------------------updateTransport(...) -----------------------------------------
    @Test
    public void successUpdateNotExistentTransport() {
        BrokerBackup brokerBackup = new BrokerBackup(primaryURL);
        String tSerialized = new Gson().toJson(transport);
        brokerBackup.updateTransport(tSerialized);

        assertNotNull("Transport not added", manager.getTransportById(transport.getId()));
    }

    @Test
    public void successUpdateExistentTransport() {
        manager.addTransport(transport);
        assertEquals(manager.getTransportById("1").getState(), TransportStateView.REQUESTED);

        Transport newT = new Transport(); newT.setId("1"); newT.setState(TransportStateView.COMPLETED);
        BrokerBackup brokerBackup = new BrokerBackup(primaryURL);
        String tSerialized = new Gson().toJson(newT);
        brokerBackup.updateTransport(tSerialized);

        assertEquals("Not update", manager.getTransportById("1").getState(), TransportStateView.COMPLETED);
    }

}
