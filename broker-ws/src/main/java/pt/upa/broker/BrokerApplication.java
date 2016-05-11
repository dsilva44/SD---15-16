package pt.upa.broker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.broker.domain.Broker;
import pt.upa.broker.domain.BrokerBackup;
import pt.upa.broker.domain.BrokerPrimary;
import pt.upa.broker.domain.Manager;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.EndpointManager;

import javax.xml.ws.WebServiceException;
import java.io.IOException;

public class BrokerApplication {
	static private final Logger log = LogManager.getRootLogger();

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 5) {
			log.error("Argument(s) missing!");
			log.error("Usage: java "+ BrokerApplication.class.getName() +" + wsURL wsName uddiURL ksPath password");
			return;
		}

		String wsURL = args[0];
		String wsName = args[1];
		String uddiURL = args[2];
		String ksPath = args[3];
		String password = args[4];

		Broker broker;
		EndpointManager epm =  new EndpointManager(wsURL, wsName, uddiURL);
		String primaryURL = epm.uddiLookup(wsName);

		if (primaryURL == null) {
			broker = new BrokerPrimary(wsURL);
			epm.registerUddi();
		} else {
			BrokerPortType primaryStub = epm.createStub(primaryURL, 2000, 2000);
			try {
				primaryStub.registerBackup(wsURL);
				broker = new BrokerBackup(primaryURL);
			} catch (WebServiceException wse) {
				log.info("Primary is down "+wse.getMessage());
				broker = new BrokerPrimary(wsURL);
				epm.registerUddi();
			}
		}

		Manager.getInstance().init(epm, broker, ksPath, password);
		epm.start();

		if (epm.awaitConnections()) {
			try {
				System.out.println("Press enter to shutdown");

				System.in.read();
			} catch (IOException e) {
				log.error("Error:: ", e);
			}
		}
		epm.stop();
	}

}
