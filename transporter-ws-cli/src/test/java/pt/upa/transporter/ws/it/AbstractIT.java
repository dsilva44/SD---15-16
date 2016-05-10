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
public abstract class AbstractIT {
    // static members
    protected static final Logger log = LogManager.getRootLogger();
    static TransporterClient CLIENT1, CLIENT2;

    static final int PRICE_UPPER_LIMIT = 100;
    static final int PRICE_SMALLEST_LIMIT = 10;
    static final int UNITARY_PRICE = 1;
    static final int ZERO_PRICE = 0;
    static final int INVALID_PRICE = -1;
    static final String CENTRO_1 = "Lisboa";
    static final String SUL_1 = "Beja";
    static final String CENTRO_2 = "Coimbra";
    static final String SUL_2 = "Portalegre";
    static final String EMPTY_STRING = "";
    static final int DELAY_LOWER = 1000; // milliseconds

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        final String uddiURL = "http://localhost:9090";
        final String wsName1 = "UpaTransporter1";
        final String wsName2 = "UpaTransporter2";
        final String brokerName = "UpaBroker";
        final String brokerKSPath = "src/main/resources/UpaBroker.jks";
        final String brokerPass = "passUpaBroker";

        CLIENT1 = new TransporterClient(uddiURL,wsName1, brokerName, brokerKSPath, brokerPass);
        CLIENT2 = new TransporterClient(uddiURL,wsName2, brokerName, brokerKSPath, brokerPass);
    }

    @AfterClass
    public static void oneTimeTearDown() {
        CLIENT1.clearJobs();
        CLIENT1 = null;

        CLIENT2.clearJobs();
        CLIENT2 = null;
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
