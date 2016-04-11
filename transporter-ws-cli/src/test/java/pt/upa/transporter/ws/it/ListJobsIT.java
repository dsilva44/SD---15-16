package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import pt.upa.transporter.ws.JobView;

public class ListJobsIT extends AbstractIntegrationTest {

    @Test
    public void successListJobsResponse() {
        // FIXME What is this ????
    	assertTrue(client.listJobs() instanceof List);
    }
}