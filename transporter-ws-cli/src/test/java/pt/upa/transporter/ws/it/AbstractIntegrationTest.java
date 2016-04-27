package pt.upa.transporter.ws.it;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import pt.upa.transporter.ws.cli.TransporterClient;

/**
 * Abstract Integration Test
 * <p>
 * Invoked by Maven in the "verify" life-cycle phase
 * Should invoke "live" remote servers
 */
public abstract class AbstractIntegrationTest {
    // static members
    protected static final Logger log = LogManager.getRootLogger();
    protected static TransporterClient client1, client2;

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        final String uddiURL = "http://localhost:9090";
        final String wsName1 = "UpaTransporter1";
        final String wsName2 = "UpaTransporter2";

        client1 = new TransporterClient(uddiURL,wsName1);
        client2 = new TransporterClient(uddiURL,wsName2);

        client1.clearJobs();
        client2.clearJobs();
    }

    @AfterClass
    public static void oneTimeTearDown() {
        client1.clearJobs();
        client2.clearJobs();
    }

    // members


    // initialization and clean-up for each test
    @Before
    public void setUp() throws Exception{
    }

    @After
    public void tearDown() {
    }
}
