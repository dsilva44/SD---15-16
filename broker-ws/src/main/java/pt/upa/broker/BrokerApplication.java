package pt.upa.broker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.broker.domain.Broker;
import pt.upa.broker.domain.BrokerBackup;
import pt.upa.broker.domain.BrokerPrimary;
import pt.upa.broker.domain.Manager;
import pt.upa.broker.ws.EndpointManager;
import java.io.IOException;

public class BrokerApplication {
	static private final Logger log = LogManager.getRootLogger();

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 5) {
			log.error("Argument(s) missing!");
			log.error("Usage: java "+ BrokerApplication.class.getName() +" + wsPrimary wsBackup wsType wsName uddiURL");
			return;
		}

		String wsPrimary = args[0];
		String wsBackup = args[1];
		String wsType = args[2];
		String wsName = args[3];
		String uddiURL = args[4];

		EndpointManager endpointManager;
		Broker broker;
		if (Integer.parseInt(wsType) == 1) {
			endpointManager = new EndpointManager(wsPrimary,  wsName);
			broker = new BrokerPrimary(uddiURL, endpointManager);
			broker.registerUddi();
		} else {
			endpointManager = new EndpointManager(wsBackup, wsName);
			broker = new BrokerBackup(uddiURL, endpointManager);
		}

		endpointManager.start();
		Manager.getInstance().init(endpointManager, broker);

		if (endpointManager.awaitConnections()) {
			try {
				System.out.println("Press enter to shutdown");

				System.in.read();
			} catch (IOException e) {
				log.error("Error:: ", e);
			}
		}
		endpointManager.stop();
		broker.deleteFromUDDI();
	}

}
