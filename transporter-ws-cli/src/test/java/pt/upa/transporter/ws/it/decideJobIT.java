package pt.upa.transporter.ws.it;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.upa.transporter.ws.BadJobFault;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class decideJobIT extends AbstractIntegrationTest{

    private static JobView jv;

    // initialization and clean-up for each test
    @Before
    public void setUp() throws Exception{
        jv = client1.requestJob("Lisboa", "Leiria", 50);
    }

    @After
    public void tearDown() {
        client1.clearJobs();
    }

    @Test(expected=BadJobFault_Exception.class)
    public void decideJobWithInvalidIDTest() throws Exception{
        client1.decideJob("bananas", true);
    }


    @Test(expected=BadJobFault_Exception.class)
    public void truedecideJobWithWrongStateTest() throws Exception {
        client1.decideJob(jv.getJobIdentifier(), true);
        client1.decideJob(jv.getJobIdentifier(), true);
    }

    @Test(expected=BadJobFault_Exception.class)
    public void falsedecideJobWithWrongStateTest() throws Exception {
        client1.decideJob(jv.getJobIdentifier(), false);
        client1.decideJob(jv.getJobIdentifier(), false);
    }

    @Test
    public void truedecideJobWithCorrectStateTest() throws Exception {
        JobView job = client1.decideJob(jv.getJobIdentifier(), true);
        assertEquals("decideJob not working correctly", job.getJobState(), JobStateView.ACCEPTED);
    }

    @Test
    public void falsedecideJobWithCorrectStateTest() throws Exception {
        JobView job = client1.decideJob(jv.getJobIdentifier(), false);
        assertEquals("decideJob not working correctly", job.getJobState(), JobStateView.REJECTED);

    }


}
