package pt.upa.transporter.ws.it;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import pt.upa.transporter.ws.TransporterPortType;
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
    protected static TransporterClient client;

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        String uddiURL = "http://localhost:9090";
        String wsName = "UpaTransporter1";
        client = new TransporterClient(uddiURL,wsName);
    }

    @AfterClass
    public static void oneTimeTearDown() {
        client = null;
    }

    // members


    // initialization and clean-up for each test
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
}
