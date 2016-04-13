package pt.upa.transporter.ws.it;


import org.junit.Test;
import pt.upa.transporter.ws.BadJobFault;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.JobView;

import java.util.List;

import static org.junit.Assert.assertTrue;


/**
 * Created by david on 11-04-2016.
 */
public class decideJobIT extends AbstractIntegrationTest{

    @Test
    public void validJobdecideJobReturnList() throws BadJobFault_Exception{
        JobView jv = client.decideJob("validjobtest", true);
        assertTrue("decideJob not returning a List", jv instanceof JobView);
    }

   @Test(expected = BadJobFault_Exception.class)
    public void invalidJobdecideJobReturnList() throws BadJobFault_Exception{
        JobView jv = client.decideJob("invalidjobtest", true);
        assertTrue("decideJob not returning a List", jv instanceof JobView);
    }
}
