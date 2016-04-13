package pt.upa.transporter.ws.it;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.upa.transporter.ws.BadJobFault;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.JobView;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class decideJobIT extends AbstractIntegrationTest{

    // initialization and clean-up for each test
    @Before
    public void setUp() throws Exception{
        client1.requestJob("Lisboa", "Leiria", 50);
    }

    @After
    public void tearDown() {
        client1.clearJobs();
    }

    @Test(expected=BadJobFault_Exception.class)
    public void decideJobWithInvalidIDTest() throws Exception{
        client1.decideJob("bananas", true);
    }
}
