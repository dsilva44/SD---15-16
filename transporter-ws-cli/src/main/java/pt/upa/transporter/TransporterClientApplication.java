package pt.upa.transporter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.cli.TransporterClient;

public class TransporterClientApplication {
	static private final Logger log = LogManager.getRootLogger();

	public static void main(String[] args) throws Exception {

		if (args.length < 2) {
			log.error("Argument(s) missing!");
			log.error("Usage: java "+ TransporterClientApplication.class.getName() +" + uddiURL wsName");
			return;
		}

		TransporterClient client = new TransporterClient(args[0], args[1]);
		TransporterPortType port = client.getPort();

		String result = port.ping("friend");
	}
}
