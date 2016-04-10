package pt.upa.transporter;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.domain.Manager;
import pt.upa.transporter.ws.TransporterPort;

import javax.xml.ws.Endpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TransporterApplication {
	static private final Logger log = LogManager.getRootLogger();

	public static void main(String[] args) {

		// Check arguments
		if (args.length < 3) {
			log.error("Argument(s) missing!");
			log.error("Usage: java "+ TransporterApplication.class.getName() +" + uddiURL wsName wsURL");
			return;
		}

		String uddiURL = args[0];
		String name = args[1];
		String url = args[2];

		Manager.getInstance().init(name);

		Endpoint endpoint = null;
		UDDINaming uddiNaming = null;
		try {
			endpoint = Endpoint.create(new TransporterPort());

			// publish endpoint
			log.info("Starting: " + url);
			endpoint.publish(url);

			// publish to UDDI
			log.info("Publishing '"+ name + "' to UDDI at "+ uddiURL);
			uddiNaming = new UDDINaming(uddiURL);
			uddiNaming.rebind(name, url);

			// wait
			log.info("Awaiting connections");
			System.out.println("Press enter to shutdown");
			System.in.read();

		} catch (Exception e) {
			log.error("Caught exception", e);
		} finally {
			try {
				if (endpoint != null) {
					// stop endpoint
					endpoint.stop();
					log.info("Stopped " + url);
				}
			} catch (Exception e) {
				log.error("Caught exception when stopping", e);
			}
			try {
				if (uddiNaming != null) {
					// delete from UDDI
					uddiNaming.unbind(name);
					log.info("Deleted '"+ name +"' from UDDI");
				}
			} catch (Exception e) {
				log.error("Caught exception when deleting", e);
			}
		}

	}
}
