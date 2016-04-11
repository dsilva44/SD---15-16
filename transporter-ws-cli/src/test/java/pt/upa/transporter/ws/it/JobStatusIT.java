package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pt.upa.transporter.ws.JobView;

public class JobStatusIT extends AbstractIntegrationTest {

    @Test
    public void successJobStatusShouldReturnJobViewOrNull() {
    	assertTrue(client.jobStatus("id") == null || client.jobStatus("id") instanceof JobView);
    }
}
