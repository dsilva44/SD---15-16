package pt.upa.transporter.ws;

import mockit.Expectations;
import mockit.Mocked;
import org.junit.*;

import javax.xml.ws.WebServiceContext;

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
    public void successPingShouldNotReturnNull(@Mocked WebServiceContext wsContext) {
        new Expectations(){{
            wsContext.getMessageContext();
        }};

        TransporterPort port = new TransporterPort();
        port.setWsContext(wsContext);
        assertNotNull("Ping return null", port.ping("friend"));
    }



}
