package pt.upa.transporter.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import pt.upa.transporter.exception.WrongStateToConfirmException;
import pt.upa.transporter.exception.JobDoesNotExistException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;

public class ManagerTest {
	
    // static members
	private static Manager m = Manager.getInstance();

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() {
        m.init("UpaTransporter1");

    }

    @AfterClass
    public static void oneTimeTearDown() {

    }


    // members
    private JobView validjob;
    private JobView invalidjob;

    // initialization and clean-up for each test
    @Before
    public void setUp() {

        validjob = new JobView();
        validjob.setCompanyName("UpaTransporter1");
        validjob.setJobDestination("Lisboa");
        validjob.setJobOrigin("Leiria");
        validjob.setJobIdentifier("validjobtest");
        validjob.setJobPrice(50);
        validjob.setJobState(JobStateView.PROPOSED);

        invalidjob = new JobView();
        invalidjob.setCompanyName("UpaTransporter1");
        invalidjob.setJobDestination("Lisboa");
        invalidjob.setJobOrigin("Leiria");
        invalidjob.setJobIdentifier("invalidjobtest");
        invalidjob.setJobPrice(50);
        invalidjob.setJobState(JobStateView.HEADING);

        m.addJob(validjob);
        m.addJob(invalidjob);
    }

    @After
    public void tearDown() {
        m.removeJob(validjob);
        m.removeJob(invalidjob);
    }

	@Test
	public void successJobListShouldBeEmpty() {
		m.setJobs(null);
        assertEquals("Job list should be empty", 0,m.getJobs().size());
    }

    @Test(expected=JobDoesNotExistException.class)
    public void ConfirmJobWithInvalidIDTest() throws Exception{

        m.confirmationJobs("bananas", true);
    }

    @Test(expected=WrongStateToConfirmException.class)
    public void trueConfirmJobWithWrongStateTest() throws Exception{

       m.confirmationJobs("invalidjobtest", true);
    }

    @Test
    public void trueConfirmJobWithCorrectStateTest() throws Exception{
        JobView job = m.confirmationJobs("validjobtest", true);

        assertEquals("confirmation job did not work correctly", job.getJobState(), JobStateView.ACCEPTED);
    }

    @Test(expected = WrongStateToConfirmException.class)
    public void falseConfirmWithWrongStateJobTest() throws Exception{
        m.confirmationJobs("invalidjobtest", false);
    }

    @Test
    public void falseConfirmWithCorrectStateJobTest() throws Exception{
        JobView job = m.confirmationJobs("validjobtest", false);

        assertEquals("confirmation job did not work correctly", job.getJobState(), JobStateView.REJECTED) ;
    }
}
