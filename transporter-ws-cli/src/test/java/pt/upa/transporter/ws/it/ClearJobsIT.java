package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;

public class ClearJobsIT extends AbstractIntegrationTest {
	
	private final String validLocation1 = "Lisboa";
    private final String validLocation2 = "Leiria";
    private final int validPrice1 = 20;
    private final int validPrice2 = 50;
    
    @Test
    public void successClearJobsShouldEmptyList() throws BadLocationFault_Exception, BadPriceFault_Exception {
    	
    	client1.requestJob(validLocation1, validLocation2, validPrice1);
    	client1.requestJob(validLocation2, validLocation1, validPrice2);
    	
    	client1.clearJobs();
        assertTrue(client1.listJobs().isEmpty());
    }

    @Test
    public void successClearJobsEmptyList() throws BadLocationFault_Exception, BadPriceFault_Exception {

        client1.clearJobs();
        assertTrue(client1.listJobs().isEmpty());
    }

}