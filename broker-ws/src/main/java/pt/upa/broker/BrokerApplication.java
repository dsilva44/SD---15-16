package pt.upa.broker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.broker.ws.EndpointManager;
import java.io.IOException;

public class BrokerApplication {
	static private final Logger log = LogManager.getRootLogger();

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 4) {
			log.error("Argument(s) missing!");
			log.error("Usage: java "+ BrokerApplication.class.getName() +" + uddiURL wsName wsURL wsType");
			return;
		}

		String uddiURL = args[0];
		String wsName = args[1];
		String wsUrl = args[2];
		String wsType = args[3];

		EndpointManager endpointManager = new EndpointManager(uddiURL, wsName, wsUrl);

		if (Integer.parseInt(wsType) == 1) endpointManager.registerUddi();
		endpointManager.start();

		if (endpointManager.awaitConnections()) {
			try {
				System.out.println("Press enter to shutdown");
				System.in.read();
			} catch (IOException e) {
				log.error("Error:: ", e);
			}
		}
		endpointManager.stop();
	}

}
