package pt.upa.broker.ws.it;

import org.junit.Test;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportStateView;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;

import static org.junit.Assert.assertEquals;

import java.util.Date;

public class ViewTransportIT extends AbstractIntegrationTest{

    private String centroCity1 = "Lisboa";
    private String centroCity2 = "Leiria";

    @Test
    public void CompletedStateAfter15Seconds() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
            UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception,
            UnknownTransportFault_Exception{

        String id = brokerClient.requestTransport(centroCity1, centroCity2, 20);
        Date init = new Date();


        while (init.getTime() + 17000 > new Date().getTime());

        TransportView tView = brokerClient.viewTransport(id);

        assertEquals("State not completed after 15 seconds",tView.getState(), TransportStateView.COMPLETED);
    }

    @Test(expected= UnknownTransportFault_Exception.class)
    public void failStateAfter15Seconds() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
            UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception,
            UnknownTransportFault_Exception{

        Date init = new Date();


        while (init.getTime() + 17000 > new Date().getTime());

        TransportView tView = brokerClient.viewTransport("hello");
    }
}
