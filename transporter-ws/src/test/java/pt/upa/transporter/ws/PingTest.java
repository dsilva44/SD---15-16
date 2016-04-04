package pt.upa.transporter.ws;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *  Unit Test Ping
 *
 *  Invoked by Maven in the "test" life-cycle phase
 *  If necessary, should invoke "mock" remote servers
 */
public class PingTest {

    @Test
    public void nullNameShouldNotReturnNull() {
        TransporterPort port = new TransporterPort();
        assertNotNull("Null Name", port.ping(null));
    }

    @Test
    public void notNullNameShouldNotReturnNull() {
        TransporterPort port = new TransporterPort();
        assertNotNull("Not Null Name", port.ping("friend"));
    }
}
