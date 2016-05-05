package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ClearJobsIT extends AbstractIT {

    @Test
    public void successClearJobsShouldEmptyList() throws Exception {

    	CLIENT1.requestJob(CENTRO_1, CENTRO_2, PRICE_SMALLEST_LIMIT);
    	CLIENT1.requestJob(CENTRO_1, SUL_1, PRICE_UPPER_LIMIT);

    	CLIENT1.clearJobs();
        assertTrue(CLIENT1.listJobs().isEmpty());
    }

    @Test
    public void successClearJobsEmptyList() throws Exception {

        CLIENT1.clearJobs();
        assertTrue(CLIENT1.listJobs().isEmpty());
    }

    /*----------------------------------------------SD-Tests---------------------------------------------------------*/
    /**
     * Delete all jobs in a transporter. There shouldn't be any errors.
     *
     * @result CLIENT.listJobs() will return 0 after invoking
     *         CLIENT.clearJobs().
     * @throws Exception
     */
    @Test
    public void testClearJob() throws Exception {
        CLIENT1.clearJobs();
        assertEquals(0, CLIENT1.listJobs().size());
    }
}