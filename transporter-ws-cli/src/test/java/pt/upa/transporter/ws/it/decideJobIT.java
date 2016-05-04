package pt.upa.transporter.ws.it;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class decideJobIT extends AbstractIT {

    private static JobView jv;

    // initialization and clean-up for each test
    @Before
    public void setUp() throws Exception{
        jv = CLIENT1.requestJob(CENTRO_1, SUL_1, PRICE_UPPER_LIMIT);
    }

    @After
    public void tearDown() {
        CLIENT1.clearJobs();
    }

    /*----------------------------------------------T_27-Tests--------------------------------------------------------*/
    @Test(expected=BadJobFault_Exception.class)
    public void decideJobWithInvalidIDTest() throws Exception{
        CLIENT1.decideJob("bananas", true);
    }


    @Test(expected=BadJobFault_Exception.class)
    public void truedecideJobWithWrongStateTest() throws Exception {
        CLIENT1.decideJob(jv.getJobIdentifier(), true);
        CLIENT1.decideJob(jv.getJobIdentifier(), true);
    }

    @Test(expected=BadJobFault_Exception.class)
    public void falsedecideJobWithWrongStateTest() throws Exception {
        CLIENT1.decideJob(jv.getJobIdentifier(), false);
        CLIENT1.decideJob(jv.getJobIdentifier(), false);
    }

    @Test
    public void truedecideJobWithCorrectStateTest() throws Exception {
        JobView job = CLIENT1.decideJob(jv.getJobIdentifier(), true);
        assertEquals("decideJob not working correctly", job.getJobState(), JobStateView.ACCEPTED);
    }

    @Test
    public void falsedecideJobWithCorrectStateTest() throws Exception {
        JobView job = CLIENT1.decideJob(jv.getJobIdentifier(), false);
        assertEquals("decideJob not working correctly", job.getJobState(), JobStateView.REJECTED);

    }

    /*----------------------------------------------SD-Tests1---------------------------------------------------------*/
    /**
     * Inform a transporter that the client (such as broker-ws) decided to
     * accept the job offer.
     *
     * @result The job's state is JobStateView.ACCEPTED.
     * @throws Exception
     */
    @Test
    public void testAcceptJob() throws Exception {
        jv = CLIENT1.decideJob(jv.getJobIdentifier(), true);
        assertEquals(JobStateView.ACCEPTED, jv.getJobState());
    }

    /**
     * Try to invoke decideJob twice on the same job.
     *
     * @result Should throw exception because after the first call to
     *         CLIENT.decideJob, the job's state is no longer
     *         JobStateView.PROPOSED.
     * @throws BadJobFault_Exception
     */
    @Test(expected = BadJobFault_Exception.class)
    public void testAcceptDuplicateJob() throws Exception {
        CLIENT1.decideJob(jv.getJobIdentifier(), true);
        CLIENT1.decideJob(jv.getJobIdentifier(), true);
    }

    /**
     * Try to invoke decideJob with an invalid (empty string) job identifier.
     *
     * @result Should throw exception because it does not make sense to decide
     *         on a job without an associated identifier.
     * @throws BadJobFault_Exception
     */
    @Test(expected = BadJobFault_Exception.class)
    public void testAcceptInvalidJob() throws Exception {
        CLIENT1.decideJob(EMPTY_STRING, true);
    }

    /**
     * Try to invoke decideJob with an invalid (null) job identifier.
     *
     * @result Should throw exception because it does not make sense to decide
     *         on a job without an associated identifier.
     * @throws BadJobFault_Exception
     */
    @Test(expected = BadJobFault_Exception.class)
    public void testAcceptNullJob() throws Exception {
        CLIENT1.decideJob(null, true);
    }

    /*----------------------------------------------SD-Tests2---------------------------------------------------------*/
    /**
     * Create a job (with valid arguments), decide on it (reject) and check that
     * its state changed to JobStateView.REJECTED.
     *
     * @throws Exception
     */
    @Test
    public void testRejectJob() throws Exception {
        JobView jv = CLIENT1.requestJob(CENTRO_1, SUL_1, PRICE_SMALLEST_LIMIT);
        jv = CLIENT1.decideJob(jv.getJobIdentifier(), false);
        assertEquals(JobStateView.REJECTED, jv.getJobState());
    }

    /**
     * Create a job (with valid arguments) and attempt to decide (reject) on it
     * twice.
     *
     * @result Should throw BadJobFault_Exception because it does not make sense
     *         to decide on an already decided job.
     * @throws Exception
     */
    @Test(expected = BadJobFault_Exception.class)
    public void testRejectDuplicateJob() throws Exception {
        JobView jv = CLIENT1.requestJob(CENTRO_2, SUL_2, PRICE_SMALLEST_LIMIT);
        CLIENT1.decideJob(jv.getJobIdentifier(), false);
        CLIENT1.decideJob(jv.getJobIdentifier(), false);
    }

    /**
     * Invoke CLIENT.decideJob on an invalid (empty string) job identifier.
     *
     * @result Should throw BadJobFault_Exception as the job is invalid.
     * @throws Exception
     */
    @Test(expected = BadJobFault_Exception.class)
    public void testRejectInvalidJob() throws Exception {
        CLIENT1.decideJob(EMPTY_STRING, false);
    }

    /**
     * Invoke CLIENT.decideJob on an invalid (null) job identifier.
     *
     * @result Should throw BadJobFault_Exception as the job is invalid.
     * @throws Exception
     */
    @Test(expected = BadJobFault_Exception.class)
    public void testRejectNullJob() throws Exception {
        CLIENT1.decideJob(null, false);
    }
}
