package pt.upa.transporter.ws.it;

import org.junit.*;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.cli.TransporterClient;

import static org.junit.Assert.*;

/**
 *  Integration Test Ping
 *
 *  Invoked by Maven in the "verify" life-cycle phase
 *  Should invoke "live" remote servers
 */
public class PingIT {

    // static members
    private static TransporterPortType port;

    // one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        String uddiURL = "http://localhost:9090";
        String wsName = "UpaTransporter1";
        TransporterClient transporterClient = new TransporterClient(uddiURL, wsName);
        port = transporterClient.getPort();
    }

    @AfterClass
    public static void oneTimeTearDown() {
        port = null;
    }


    // tests

    @Test
    public void nullNameShouldNotReturnNull() {
        assertNotNull("Null Name", port.ping(null));
    }

    @Test
    public void notNullNameShouldNotReturnNull() {
        assertNotNull("Not Null Name", port.ping("friend"));
    }

}