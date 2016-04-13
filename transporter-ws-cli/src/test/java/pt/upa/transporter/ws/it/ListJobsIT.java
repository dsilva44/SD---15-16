package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;

public class ListJobsIT extends AbstractIntegrationTest {
	
	private String validLocation1 = "Lisboa";
    private String validLocation2 = "Leiria";
    private String invalidLocation = "Paradise";
    
    private int validPrice1 = 20;
    private int validPrice2 = 50;
    
    @Test
    public void successListJobsWithListEmpty() throws BadLocationFault_Exception, BadPriceFault_Exception {    	
    	assertEquals(0,client1.listJobs().size());
    }
    
    @Test
    public void successListJobsWithListNonEmpty() throws BadLocationFault_Exception, BadPriceFault_Exception {
    	client1.requestJob(validLocation1, validLocation2, validPrice1);
    	client1.requestJob(validLocation2, validLocation1, validPrice2);



        assertEquals(2,client1.listJobs().size());
    }
    
}