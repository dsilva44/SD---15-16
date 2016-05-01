package pt.upa.ca.ws.it;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import pt.upa.ca.ws.cli.CAClient;

/**
 * Abstract Integration Test
 * <p>
 * Invoked by Maven in the "verify" life-cycle phase
 * Should invoke "live" remote servers
 */
public abstract class AbstractIT {
    // static members
    static final Logger log = LogManager.getRootLogger();
    static CAClient CLIENT;

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        String uddiURL = "http://localhost:9090";

        CLIENT = new CAClient(uddiURL);
    }

    @AfterClass
    public static void oneTimeTearDown() {
        CLIENT = null;
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
