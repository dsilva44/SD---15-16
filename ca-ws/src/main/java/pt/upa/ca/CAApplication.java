package pt.upa.ca;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.ca.domain.Manager;
import pt.upa.ca.ws.EndpointManager;
import java.io.IOException;

public class CAApplication {
    static private final Logger log = LogManager.getRootLogger();

    public static void main(String[] args) throws Exception {
        // Check arguments
        if (args.length < 4) {
            log.error("Argument(s) missing!");
            log.error("Usage: java "+ CAApplication.class.getName() +" + uddiURL wsName wsURL keyStorePath");
            return;
        }

        String uddiURL = args[0];
        String wsName = args[1];
        String wsUrl = args[2];
        String keyStorePath = args[3];

        EndpointManager endpointManager = new EndpointManager(uddiURL, wsUrl);

        endpointManager.start();

        Manager.getInstance().init(wsName, keyStorePath);

        if (endpointManager.awaitConnections()) {
            try {
                System.out.println("Press enter to shutdown");
                System.in.read();
            } catch (IOException e) {
                log.error("Error: ", e);
            }
        }
        endpointManager.stop();
    }
}
