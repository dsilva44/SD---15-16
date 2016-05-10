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
		if (args.length < 3) {
			log.error("Argument(s) missing!");
			log.error("Usage: java "+ BrokerApplication.class.getName() +" + wsURL wsName uddiURL");
			return;
		}

		String wsURL = args[0];
		String wsName = args[1];
		String uddiURL = args[2];

		Broker broker;
		EndpointManager emp =  new EndpointManager(wsURL, wsName, uddiURL);
		String primaryURL = emp.uddiLookup(wsName);

		if (primaryURL == null) {
			broker = new BrokerPrimary(wsURL);
			emp.registerUddi();
		} else {
			BrokerPortType primaryStub = emp.createStub(primaryURL, 2000, 2000);
			try {
				primaryStub.registerBackup(wsURL);
				broker = new BrokerBackup(primaryURL);
			} catch (WebServiceException wse) {
				log.info("Primary is down "+wse.getMessage());
				broker = new BrokerPrimary(wsURL);
				emp.registerUddi();
			}
		}

		Manager.getInstance().init(emp, broker);
		emp.start();

		if (emp.awaitConnections()) {
			try {
				System.out.println("Press enter to shutdown");

				System.in.read();
			} catch (IOException e) {
				log.error("Error:: ", e);
			}
		}
		emp.stop();
	}

}
