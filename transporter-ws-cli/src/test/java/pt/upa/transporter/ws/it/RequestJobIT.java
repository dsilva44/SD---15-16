package pt.upa.transporter.ws.it;

import org.junit.Test;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;

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

        client2.requestJob(centroLocation2, unknownLocation, referencePrice);
    }

    @Test(expected = BadPriceFault_Exception.class)
    public void negativePriceShouldThewException() throws BadLocationFault_Exception, BadPriceFault_Exception  {
        int referencePrice = -100;

        client3.requestJob(centroLocation1, centroLocation2, referencePrice);
    }

}
