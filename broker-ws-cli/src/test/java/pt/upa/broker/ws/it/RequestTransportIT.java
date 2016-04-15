package pt.upa.broker.ws.it;

import org.apache.juddi.v3.client.transport.Transport;
import org.junit.Test;
import pt.upa.broker.ws.*;

public class RequestTransportIT extends AbstractIntegrationTest {

    private String centroCity1 = "LisBOA";
    private String centroCity2 = "LeIRia";

    @Test(expected = UnknownLocationFault_Exception.class)
    public void unknownOrigin()
            throws  Exception{
        String invalidDestination = "PoTATO";
        int price = 50;

        brokerClient.requestTransport(invalidDestination, centroCity1, price);
    }

    @Test(expected = UnknownLocationFault_Exception.class)
    public void unknownDestination()
            throws  Exception {
        String invalidOrigin = null;
        int price = 50;

        brokerClient.requestTransport(centroCity2, invalidOrigin, price);
    }

    @Test(expected = InvalidPriceFault_Exception.class)
    public void invalidPrice()
            throws InvalidPriceFault_Exception, UnknownLocationFault_Exception, UnavailableTransportFault_Exception,
            UnavailableTransportPriceFault_Exception {
        int price = -50;

        brokerClient.requestTransport(centroCity1, centroCity2, price);
    }

    @Test(expected = UnavailableTransportFault_Exception.class)
    public void allNullTransportersResponse()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception {
        brokerClient.requestTransport(centroCity1, centroCity2, 100);
    }

    @Test(expected = UnavailableTransportFault_Exception.class)
    public void referencePrice0ShouldThrowException()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception {
        brokerClient.requestTransport(centroCity1, centroCity2, 0);
    }

    @Test
    public void successEvenPriceRequestTransporter()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception {
        brokerClient.requestTransport("Lisboa", "Leiria", 15);
    }
}
