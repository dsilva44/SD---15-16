package pt.upa.transporter.domain;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import pt.upa.transporter.exception.WrongStateToConfirmException;
import pt.upa.transporter.exception.JobDoesNotExistException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pt.upa.transporter.ws.JobStateView;

import java.util.ArrayList;
import java.util.Arrays;

public class ManagerTest {
	
    // static members

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() {
    }

    @AfterClass
    public static void oneTimeTearDown() {

    }


    // members
    private Manager manager = Manager.getInstance();
    private Job validJob = new Job("UpaTransporter1", "validjobtest", "Lisboa", "Leiria", 50, JobStateView.PROPOSED);
    private Job invalidJob = new Job( "UpaTransporter1", "invalidjobtest", "Lisboa", "Leiria", 50, JobStateView.HEADING);

    // initialization and clean-up for each test
    @Before
    public void setUp() {
        manager.addJob(validJob);
        manager.addJob(invalidJob);
    }

    @After
    public void tearDown() {
        manager.removeJob(validJob);
        manager.removeJob(invalidJob);
    }

	@Test
	public void successSetJobsShouldClearList() {
		Job job1 = new Job();
		manager.addJob(job1);

		manager.setJobs(null);
        assertEquals(0, manager.getJobs().size());
    }

    @Test
    public void successODDTransporterInit() {
        String validTransporterName = "UpaTransporter983";
        ArrayList<String> workCities = new ArrayList<>(Arrays.asList(
                "Lisboa", "Leiria", "Santarém", "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda",
                "Setúbal", "Évora", "Portalegre", "Beja", "Faro"));

        manager.init(validTransporterName);

        assertEquals("wrong parity", "ODD", manager.getParity());
        assertEquals("wrong work cities", workCities, manager.getWorkCities());
    }

    @Test
    public void successEVENTransporterInit() {
        String validTransporterName = "UpaTransporter4864";
        ArrayList<String> workCities = new ArrayList<>(Arrays.asList(
                "Lisboa", "Leiria", "Santarém", "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda",
                "Porto", "Braga", "Viana do Castelo", "Vila Real", "Bragança"));

        manager.init(validTransporterName);

        assertEquals("wrong parity", "EVEN", manager.getParity());
        assertEquals("wrong work cities", workCities, manager.getWorkCities());
    }

    @Test
	public void successGetJobViewExisting() {
		Job job1 = new Job();
		job1.setJobIdentifier("id1");
		manager.addJob(job1);

		assertEquals(job1, manager.getJobById("id1"));
	}

	@Test
	public void successGetJobViewNonExisting(){
		assertNull(manager.getJobById("id"));
	}


    @Test(expected=JobDoesNotExistException.class)
    public void ConfirmJobWithInvalidIDTest() throws Exception{

        manager.confirmationJobs("bananas", true);
    }

    @Test(expected=WrongStateToConfirmException.class)
    public void trueConfirmJobWithWrongStateTest() throws Exception {

       manager.confirmationJobs("invalidjobtest", true);
    }

    @Test
    public void trueConfirmJobWithCorrectStateTest() throws Exception{
        Job job = manager.confirmationJobs("validjobtest", true);

        assertEquals("confirmation job did not work correctly", job.getJobState(), JobStateView.ACCEPTED);
    }

    @Test(expected = WrongStateToConfirmException.class)
    public void falseConfirmWithWrongStateJobTest() throws Exception{
        manager.confirmationJobs("invalidjobtest", false);
    }

    @Test
    public void falseConfirmWithCorrectStateJobTest() throws Exception{
        Job job = manager.confirmationJobs("validjobtest", false);

        assertEquals("confirmation job did not work correctly", job.getJobState(), JobStateView.REJECTED) ;
    }
}
