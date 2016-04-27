package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;

public class JobStatusIT extends AbstractIntegrationTest {

	private final String validLocation1 = "Lisboa";
    private final String invalidLocation = "Paradise";
    
    private int validPrice1 = 20;
    
    @Test
    public void successJobStatusShouldReturnNull() throws BadLocationFault_Exception, BadPriceFault_Exception {
    	assertNull(client1.jobStatus("NonExistingID"));
    }
    @Test
    public void successJobStatusShouldReturnValidState() throws BadLocationFault_Exception, BadPriceFault_Exception {
    	
    	JobView job = client1.requestJob(validLocation1, validLocation1, validPrice1);
    	assertEquals(client1.jobStatus(job.getJobIdentifier()).getJobState(),job.getJobState());
    }
}
