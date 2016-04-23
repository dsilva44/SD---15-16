package pt.upa.broker.ws.it;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import pt.upa.broker.ws.*;

public class RequestTransportIT extends AbstractIntegrationTest {

    private String centroCity1 = "Lisboa";
    private String centroCity2 = "Leiria";
    private String sulCity1 = "Faro";
    private String sulCity2 = "Beja";
    private String norteCity1 = "Porto";
    private String norteCity2 = "BRAGA";

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
    public void allNullTransportersResponseCenter()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception {
        brokerClient.requestTransport(centroCity1, centroCity2, 101);
    }

    @Test(expected = UnavailableTransportFault_Exception.class)
    public void allNullTransportersResponseSouth()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception {
        brokerClient.requestTransport(sulCity1, sulCity2, 120);
    }

    @Test(expected = UnavailableTransportFault_Exception.class)
    public void allNullTransportersResponseNorth()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception {
        brokerClient.requestTransport(norteCity1, norteCity2, 130);
    }

    @Test
    public void referencePrice0ShouldReturn0()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception, UnknownTransportFault_Exception {
        String tID = brokerClient.requestTransport(centroCity1, centroCity2, 0);

        TransportView tView = brokerClient.viewTransport(tID);
        assertTrue("job price is not zero", tView.getPrice().equals(0));
    }

    @Test
    public void referencePrice1ShouldReturn0()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception, UnknownTransportFault_Exception {
        String tID = brokerClient.requestTransport(centroCity1, centroCity1, 1);

        TransportView tView = brokerClient.viewTransport(tID);
        assertTrue("job price is not zero", tView.getPrice().equals(0));
    }

    @Test
    public void referencePrice10ShouldReturnPriceBelow10()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception, UnknownTransportFault_Exception {
        String tID = brokerClient.requestTransport(centroCity1, centroCity2, 10);

        TransportView tView = brokerClient.viewTransport(tID);
        assertTrue("job price is not less them reference price", tView.getPrice() < 10);
    }

    @Test
    public void reference5ShouldReturnPriceBelow5()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception, UnknownTransportFault_Exception {
        String tID = brokerClient.requestTransport(centroCity1, centroCity2, 5);

        TransportView tView = brokerClient.viewTransport(tID);
        assertTrue("job price is not less them reference price", tView.getPrice() < 5);
    }

    @Test
    public void successEvenPriceRequestTransporterCenter()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception, UnknownTransportFault_Exception {
        String tID = brokerClient.requestTransport(centroCity1, centroCity2, 88);

        TransportView tView = brokerClient.viewTransport(tID);
        assertTrue("job price is not less them reference price", tView.getPrice() < 88);
    }

    @Test
    public void successODDPriceRequestTransporterCenter()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception, UnknownTransportFault_Exception {
        String tID = brokerClient.requestTransport(centroCity1, centroCity2, 72);

        TransportView tView = brokerClient.viewTransport(tID);
        assertTrue("job price is not less them reference price", tView.getPrice() < 72);
    }

    @Test(expected = UnavailableTransportPriceFault_Exception.class)
    public void successEvenPriceRequestTransporterSouth()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception, UnknownTransportFault_Exception {
        String tID = brokerClient.requestTransport(sulCity1, sulCity2, 98);

        TransportView tView = brokerClient.viewTransport(tID);
        assertTrue("job price is not less them reference price", tView.getPrice() < 98);
    }

    @Test
    public void successODDPriceRequestTransporterSouth()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception, UnknownTransportFault_Exception {
        String tID = brokerClient.requestTransport(sulCity1, sulCity2, 99);

        TransportView tView = brokerClient.viewTransport(tID);
        assertTrue("job price is not less them reference price", tView.getPrice() < 99);
    }

    @Test
    public void successEvenPriceRequestTransporterNorth()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception, UnknownTransportFault_Exception {
        String tID = brokerClient.requestTransport(norteCity1, norteCity2, 12);

        TransportView tView = brokerClient.viewTransport(tID);
        assertTrue("job price is not less them reference price", tView.getPrice() < 12);
    }

    @Test(expected = UnavailableTransportPriceFault_Exception.class)
    public void successODDPriceRequestTransporterNorth()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception, UnknownTransportFault_Exception {
        String tID = brokerClient.requestTransport(norteCity1, norteCity2, 13);

        TransportView tView = brokerClient.viewTransport(tID);
        assertTrue("job price is not less them reference price", tView.getPrice() < 13);
    }

    @Test(expected = UnavailableTransportPriceFault_Exception.class)
    public void successEvenPriceRequestTransporterCenterSouth()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception, UnknownTransportFault_Exception {
        String tID = brokerClient.requestTransport(centroCity1, sulCity2, 18);

        TransportView tView = brokerClient.viewTransport(tID);
        assertTrue("job price is not less them reference price", tView.getPrice() < 18);
    }

    @Test
    public void successOddPriceRequestTransporterCenterSouth()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception, UnknownTransportFault_Exception {
        String tID = brokerClient.requestTransport(sulCity1, centroCity2, 13);

        TransportView tView = brokerClient.viewTransport(tID);
        assertTrue("job price is not less them reference price", tView.getPrice() < 13);
    }

    @Test(expected = UnavailableTransportPriceFault_Exception.class)
    public void successODDPriceRequestTransporterCenterNorth()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception, UnknownTransportFault_Exception {
        String tID = brokerClient.requestTransport(norteCity1, centroCity2, 19);

        TransportView tView = brokerClient.viewTransport(tID);
        assertTrue("job price is not less them reference price", tView.getPrice() < 19);
    }

    @Test
    public void successEvenPriceRequestTransporterCenterNorth()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception, UnknownTransportFault_Exception {
        String tID = brokerClient.requestTransport(centroCity1, norteCity2, 20);

        TransportView tView = brokerClient.viewTransport(tID);
        assertTrue("job price is not less them reference price", tView.getPrice() < 20);
    }

    @Test
    public void successCaseInsensitiveOriginDestinationRequestTransporter()
            throws UnavailableTransportPriceFault_Exception, UnavailableTransportFault_Exception,
            UnknownLocationFault_Exception, InvalidPriceFault_Exception, UnknownTransportFault_Exception {
        String tID = brokerClient.requestTransport("LisBOA", "LEIria", 10);

        TransportView tView = brokerClient.viewTransport(tID);
        assertTrue("job price is not less them reference price", tView.getPrice() < 10);
    }
}
