package pt.upa.broker.ws.it;

import static junit.framework.TestCase.assertNotNull;
import org.junit.Test;
import pt.upa.broker.ws.*;

public class RequestTransportIT extends AbstractIntegrationTest {

    private String centroCity1 = "Lisboa";
    private String centroCity2 = "Leiria";

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
        int price = 50;

        brokerClient.requestTransport(centroCity2, null, price);
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
        brokerClient.requestTransport(centroCity1, centroCity2, 101);
    }

    @Test(expected = UnavailableTransportFault_Exception.class)
    public void referencePrice0ShouldMakeTransportersRejectTransport()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception {
        brokerClient.requestTransport(centroCity1, centroCity2, 0);
    }

    @Test(expected = UnavailableTransportPriceFault_Exception.class)
    public void referencePriceOneShouldMakeTransportersToOfferSamePrice()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception {
        String result = brokerClient.requestTransport(centroCity1, centroCity1, 1);

        assertNotNull("response is null", result);
    }

    @Test
    public void successRequestTransporter()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception {
        String result = brokerClient.requestTransport(centroCity1, centroCity2, 5);

        assertNotNull("response is null", result);
    }

    @Test
    public void successEvenPriceRequestTransporter()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception {
        String result = brokerClient.requestTransport(centroCity1, centroCity2, 15);

        assertNotNull("response is null", result);
    }

    @Test
    public void successODDPriceRequestTransporter()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception {
        String result = brokerClient.requestTransport(centroCity1, centroCity2, 16);

        assertNotNull("response is null", result);
    }

    @Test
    public void successCaseInsensitiveOriginDestinationRequestTransporter()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception {
        String result = brokerClient.requestTransport("LisBOA", "LEIria", 10);

        assertNotNull("response is null", result);
    }
}
