package pt.upa.broker.ws.it;

import org.junit.Test;
import pt.upa.broker.ws.TransportView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UpdateTransportIT extends AbstractIT {

    @Test
    public void successReplication() throws Exception {
        String tID_0 = CLIENT.requestTransport(CENTER_1, CENTER_2, ZERO_PRICE);
        String tID_1 = CLIENT.requestTransport(CENTER_1, CENTER_2, UNITARY_PRICE);
        String tID_2 = CLIENT.requestTransport(CENTER_1, CENTER_2, PRICE_SMALLEST_LIMIT-2);
        String tID_3 = CLIENT.requestTransport(CENTER_1, CENTER_2, PRICE_SMALLEST_LIMIT-3);
        String tID_4 = CLIENT.requestTransport(CENTER_1, CENTER_2, PRICE_SMALLEST_LIMIT-4);
        String tID_5 = CLIENT.requestTransport(CENTER_1, CENTER_2, PRICE_SMALLEST_LIMIT-5);
        String tID_6 = CLIENT.requestTransport(CENTER_1, CENTER_2, PRICE_SMALLEST_LIMIT-6);
        String tID_7 = CLIENT.requestTransport(CENTER_1, CENTER_2, PRICE_SMALLEST_LIMIT-7);
        String tID_8 = CLIENT.requestTransport(CENTER_1, CENTER_2, PRICE_SMALLEST_LIMIT-8);
        String tID_9 = CLIENT.requestTransport(CENTER_1, CENTER_2, PRICE_SMALLEST_LIMIT-9);

        log.debug("####################################### KILL Primary #############################################");
        Thread.sleep(3000);

        TransportView tView_0 = CLIENT.viewTransport(tID_0);
        TransportView tView_1 = CLIENT.viewTransport(tID_1);
        TransportView tView_2 = CLIENT.viewTransport(tID_2);
        TransportView tView_3 = CLIENT.viewTransport(tID_3);
        TransportView tView_4 = CLIENT.viewTransport(tID_4);
        TransportView tView_5 = CLIENT.viewTransport(tID_5);
        TransportView tView_6 = CLIENT.viewTransport(tID_6);
        TransportView tView_7 = CLIENT.viewTransport(tID_7);
        TransportView tView_8 = CLIENT.viewTransport(tID_8);
        TransportView tView_9 = CLIENT.viewTransport(tID_9);


        assertEquals(ZERO_PRICE, tView_0.getPrice().intValue());
        assertEquals(ZERO_PRICE, tView_1.getPrice().intValue());
        assertTrue(tView_2.getPrice() < 10);
        assertTrue(tView_3.getPrice() < 10);
        assertTrue(tView_4.getPrice() < 10);
        assertTrue(tView_5.getPrice() < 10);
        assertTrue(tView_6.getPrice() < 10);
        assertTrue(tView_7.getPrice() < 10);
        assertTrue(tView_8.getPrice() < 10);
        assertTrue(tView_9.getPrice() < 10);
    }

}
