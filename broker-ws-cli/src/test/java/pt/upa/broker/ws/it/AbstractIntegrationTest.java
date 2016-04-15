package pt.upa.broker.ws.it;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import pt.upa.broker.ws.cli.BrokerClient;

/**
 * Abstract Integration Test
 * <p>
 * Invoked by Maven in the "verify" life-cycle phase
 * Should invoke "live" remote servers
 */
public abstract class AbstractIntegrationTest {
    // static members
    protected static final Logger log = LogManager.getRootLogger();
    protected static BrokerClient brokerClient;

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        String uddiURL = "http://localhost:9090";

        brokerClient = new BrokerClient(uddiURL);
    }

    @AfterClass
    public static void oneTimeTearDown() {

        brokerClient = null;
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
