package pt.upa.transporter.ws.it;

import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;

public class PingIT extends AbstractIntegrationTest {

    @Test
    public void successPingShouldNotReturnNull() {
        assertNotNull("ping return null", CLIENT1.ping("friend"));
    }
}
