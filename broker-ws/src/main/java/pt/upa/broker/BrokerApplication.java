package pt.upa.broker;

import pt.upa.transporter.ws.cli.TransporterClient;

public class BrokerApplication {

	public static void main(String[] args) throws Exception {
		System.out.println(BrokerApplication.class.getSimpleName() + " starting...");

		// Example of the project organization
		// UML - http://goo.gl/nkmCh1
		String uddiURL = "http://localhost:9090";
		String wsName = "UpaTransporter1";
		TransporterClient client = new TransporterClient(uddiURL, wsName);

		System.out.println(client.ping("friend"));
	}

}
