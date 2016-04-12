package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pt.upa.transporter.ws.JobView;

public class JobStatusIT extends AbstractIntegrationTest {

    @Test
    public void successJobStatusShouldReturnJobViewOrNull() {
        //FIXME What is This ???
    	assertTrue(client1.jobStatus("id") == null || client1.jobStatus("id") instanceof JobView);
    }
}
