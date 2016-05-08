package pt.upa.broker.ws.it;

import org.junit.Test;
import pt.upa.broker.ws.UnknownTransportFault_Exception;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertEquals;

public class ClearTransportsIT extends AbstractIT {

    /*----------------------------------------------T_27-Tests--------------------------------------------------------*/
    @Test
    public void clearTransportsRequestedNotEmpty() throws Exception{
        CLIENT.requestTransport(CENTER_1, CENTER_2, PRICE_UPPER_LIMIT);

        assertFalse(CLIENT.listTransports().isEmpty());

        CLIENT.clearTransports();
    }

    /*----------------------------------------------SD-Tests1---------------------------------------------------------*/
    // tests
    // assertEquals(expected, actual);

    // public void clearTransports();

    @Test(expected = UnknownTransportFault_Exception.class)
    public void testClearTransports() throws Exception {
        String rt = CLIENT.requestTransport(CENTER_1, SOUTH_1, PRICE_SMALLEST_LIMIT);
        CLIENT.clearTransports();
        assertEquals(0, CLIENT.listTransports().size());
        CLIENT.viewTransport(rt);
    }
}
