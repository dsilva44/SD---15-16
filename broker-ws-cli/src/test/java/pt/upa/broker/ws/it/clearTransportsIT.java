package pt.upa.broker.ws.it;

import junit.framework.Assert;
import org.junit.Test;

import pt.upa.broker.ws.*;

import static junit.framework.TestCase.*;

public class clearTransportsIT extends AbstractIntegrationTest {

    @Test
    public void clearTransportsRequestedNotEmpty() throws Exception{

       String id = brokerClient.requestTransport("Lisboa", "Leiria", 50);

        assertFalse("listas no transporter nao actualizada", brokerClient.listTransports().isEmpty());

        brokerClient.clearTransports();

        assertTrue("lista nao apagada", brokerClient.listTransports().isEmpty());
    }

}
