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
        assertEquals("Null Name", "Pong!", port.ping(null));
    }

    @Test
    public void notNullNameShouldNotReturnNull() {
        TransporterPort port = new TransporterPort();
        assertEquals("Not Null Name", "Pong Friend!", port.ping("Friend"));
    }
}
