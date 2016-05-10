package pt.upa.transporter;

import pt.upa.transporter.domain.Manager;
import pt.upa.transporter.ws.EndpointManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class TransporterApplication {
	static private final Logger log = LogManager.getRootLogger();

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 3) {
			log.error("Argument(s) missing!");
			log.error("Usage: java "+ TransporterApplication.class.getName() +" + uddiURL wsName wsURL keyStorePath pass");
			return;
		}

		String uddiURL = args[0];
		String wsName = args[1];
		String wsUrl = args[2];
		String keyStorePath = args[3];
		String pass = args[4];

		EndpointManager endpointManager = new EndpointManager(uddiURL, wsName, wsUrl);

		endpointManager.start();

		Manager.getInstance().init(wsName, keyStorePath, pass);

		if (endpointManager.awaitConnections()) {
			try {
				System.out.println("Press enter to shutdown");
				System.in.read();
			} catch (IOException e) {
				log.error("Error ", e);
			}
		}
		endpointManager.stop();
	}
}
