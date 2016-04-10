package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ClearJobsIT extends AbstractIntegrationTest {

    @Test
    public void successClearJobsShouldEmptyList() {
    	client.clearJobs();
    	
        assertEquals(0, client.listJobs().size());
    }
}