package pt.upa.transporter.ws;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *  Unit Test TransporterPort
 *
 *  Invoked by Maven in the "test" life-cycle phase
 *  If necessary, should invoke "mock" remote servers
 */
public class TransporterPortTest {
    // static members

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() {
    }

    @AfterClass
    public static void oneTimeTearDown() {

    }


    // members

    // initialization and clean-up for each test
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    // tests
    @Test
    public void successPingShouldNotReturnNull() {
        TransporterPort port = new TransporterPort();
        assertNotNull("Ping return null", port.ping("friend"));
    }

}
