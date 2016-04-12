package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ClearJobsIT extends AbstractIntegrationTest {

    @Test
    public void successClearJobsShouldEmptyList() {
    	client1.clearJobs();
        assertTrue(client1.listJobs().isEmpty());
    }
}