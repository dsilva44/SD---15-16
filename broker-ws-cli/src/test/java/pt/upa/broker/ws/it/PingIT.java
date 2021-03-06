package pt.upa.broker.ws.it;

import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;

public class PingIT extends AbstractIT {

    @Test
    public void successPingShouldNotReturnNull() {
        String result = CLIENT.ping("friend");

        assertNotNull("ping return null", result);
        log.info(result);
    }

    @Test
    public void inputNullPingShouldNotReturnNull() {
        String result = CLIENT.ping(null);

        assertNotNull("ping return null", result);
    }

}
