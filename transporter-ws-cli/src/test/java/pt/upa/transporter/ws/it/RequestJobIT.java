package pt.upa.transporter.ws.it;

import org.junit.Test;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RequestJobIT extends AbstractIntegrationTest {

    private String centroLocation1 = "Lisboa";
    private String centroLocation2 = "Leiria";
    private String unknownLocation = "BATATA";

    @Test(expected = BadLocationFault_Exception.class)
    public void unknownOriginShouldThewException() throws BadLocationFault_Exception, BadPriceFault_Exception {
        int referencePrice = 50;

        client1.requestJob(unknownLocation, centroLocation1, referencePrice);
    }

    @Test(expected = BadLocationFault_Exception.class)
    public void unknownDestinationShouldThewException() throws BadLocationFault_Exception, BadPriceFault_Exception  {
        int referencePrice = 50;

        client1.requestJob(centroLocation2, unknownLocation, referencePrice);
    }

    @Test(expected = BadPriceFault_Exception.class)
    public void negativePriceShouldThewException() throws BadLocationFault_Exception, BadPriceFault_Exception  {
        int referencePrice = -100;

        client1.requestJob(centroLocation1, centroLocation2, referencePrice);
    }

    @Test
    public void shouldReturnNullOnInvalidWorkZoneOrigin() throws BadLocationFault_Exception, BadPriceFault_Exception  {
        int referencePrice = 50;
        String evenLocation = "Braga";
        JobView returnRequestJob = client1.requestJob(evenLocation, centroLocation1, referencePrice);

        assertNull("not return null", returnRequestJob);
    }

    @Test
    public void shouldReturnNullOnInvalidWorkZoneDestination() throws BadLocationFault_Exception, BadPriceFault_Exception  {
        int referencePrice = 50;
        String oddLocation = "Faro";

        JobView returnRequestJob = client2.requestJob(centroLocation1, oddLocation, referencePrice);

        assertNull("not return null", returnRequestJob);
    }

    @Test
    public void shouldReturnNullOnPriceGreaterThan100() throws BadLocationFault_Exception, BadPriceFault_Exception  {
        int referencePrice = 101;

        JobView returnRequestJob = client1.requestJob(centroLocation1, centroLocation2, referencePrice);

        assertNull("not return null", returnRequestJob);
    }

    @Test
    public void priceEqualTo10shouldReturnPriceLessThen10AndGreaterEqualTo0()
            throws BadLocationFault_Exception , BadPriceFault_Exception {
        int referencePrice = 10;

        JobView returnRequestJob = client1.requestJob(centroLocation1, centroLocation2, referencePrice);

        assertTrue("price is not less then 10", (returnRequestJob.getJobPrice() >= 0) &
                (returnRequestJob.getJobPrice() < 10));

    }

    @Test
    public void priceLessThen10shouldReturnPriceLessThen10AndGreaterEqualTo0()
            throws BadLocationFault_Exception, BadPriceFault_Exception  {
        int referencePrice = 5;

        JobView returnRequestJob = client1.requestJob(centroLocation1, centroLocation2, referencePrice);

        assertTrue("price is not less then 10", returnRequestJob.getJobPrice() >= 0 &
                (returnRequestJob.getJobPrice() < 10));
    }

    @Test
    public void priceEqualTo0shouldReturn0()
            throws BadLocationFault_Exception, BadPriceFault_Exception  {

        JobView returnRequestJob = client1.requestJob(centroLocation1, centroLocation2, 0);

        assertTrue("price is not less then 10", returnRequestJob.getJobPrice() == 0);
    }

    @Test
    public void oddPriceGreaterThan10LessOrEqualTo100AndOddTransporterShouldReturnPriceBelowReference()
            throws BadLocationFault_Exception, BadPriceFault_Exception {
        int referencePrice = 99;

        JobView returnRequestJob = client1.requestJob(centroLocation1, centroLocation2, referencePrice);

        assertTrue("price is not less then reference price", returnRequestJob.getJobPrice() >= 0 &
                (returnRequestJob.getJobPrice() < referencePrice));
    }

    @Test
    public void evenPriceGreaterThan10LessOrEqualTo100AndEvenTransporterShouldReturnPriceBelowReference()
            throws BadLocationFault_Exception, BadPriceFault_Exception {
        int referencePrice = 100;

        JobView returnRequestJob = client2.requestJob(centroLocation1, centroLocation2, referencePrice);

        assertNotNull("job is null", returnRequestJob);
        assertTrue("price is not less then reference price", returnRequestJob.getJobPrice() >= 0 &
                (returnRequestJob.getJobPrice() < referencePrice));
    }

    @Test
    public void oddPriceGreaterThan10LessOrEqualTo100AndEvenTransporterShouldReturnPriceAboveReference()
            throws BadLocationFault_Exception, BadPriceFault_Exception {
        int referencePrice = 11;

        JobView returnDecideResponse = client2.requestJob(centroLocation1, centroLocation2, referencePrice);

        assertTrue("price is not above reference price", returnDecideResponse.getJobPrice() >= 0 &
                (returnDecideResponse.getJobPrice() > referencePrice));
    }

    @Test
    public void evenPriceGreaterThan10LessOrEqualTo100AndOddTransporterShouldReturnPriceAboveReference()
            throws BadLocationFault_Exception, BadPriceFault_Exception {
        int referencePrice = 16;

        JobView returnDecideResponse = client1.requestJob(centroLocation1, centroLocation2, referencePrice);

        assertTrue("price is not greater reference price", returnDecideResponse.getJobPrice() >= 0 &
                (returnDecideResponse.getJobPrice() > referencePrice));
    }

}
