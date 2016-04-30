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
public abstract class AbstractIT {
    // static members
    static final Logger log = LogManager.getRootLogger();
    static BrokerClient CLIENT;

    static int PRICE_UPPER_LIMIT = 100;
    static int PRICE_SMALLEST_LIMIT = 10;

    static int INVALID_PRICE = -1;
    static int ZERO_PRICE = 0;
    static int UNITARY_PRICE = 1;

    static int ODD_INCREMENT = 1;
    static int EVEN_INCREMENT = 2;

    static final String SOUTH_1 = "Beja";
    static final String SOUTH_2 = "Portalegre";

    static final String CENTER_1 = "Lisboa";
    static final String CENTER_2 = "Coimbra";

    static final String NORTH_1 = "Porto";
    static final String NORTH_2 = "Braga";

    static final String EMPTY_STRING = "";

    static final int DELAY_LOWER = 2000; // = 2 second
    static final int DELAY_UPPER = 5000; // = 5 seconds

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        String uddiURL = "http://localhost:9090";

        CLIENT = new BrokerClient(uddiURL, "UpaBroker");
    }

    @AfterClass
    public static void oneTimeTearDown() {
        CLIENT.clearTransports();
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
