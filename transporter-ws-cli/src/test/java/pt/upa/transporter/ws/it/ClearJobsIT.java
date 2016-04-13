package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;

public class ClearJobsIT extends AbstractIntegrationTest {
	
	private String validLocation1 = "Lisboa";
    private String validLocation2 = "Leiria";
    private int validPrice1 = 20;
    private int validPrice2 = 50;
    
    @Test
    public void successClearJobsShouldEmptyList() throws BadLocationFault_Exception, BadPriceFault_Exception {
    	
    	client1.requestJob(validLocation1, validLocation2, validPrice1);
    	client1.requestJob(validLocation2, validLocation1, validPrice2);
    	
    	client1.clearJobs();
        assertTrue(client1.listJobs().isEmpty());
    }
}