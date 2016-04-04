package pt.upa.transporter.ws.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;

import javax.xml.ws.BindingProvider;
import java.util.Map;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

public class TransporterClient {
	static private final Logger log = LogManager.getRootLogger();

	private String uddiURL;
	private String wsName;
	private TransporterPortType port;

	public TransporterClient(String uddiURL, String wsName) throws Exception {
		this.uddiURL = uddiURL;
		this.wsName = wsName;

		log.info("Contacting UDDI at " + uddiURL);
		UDDINaming uddiNaming = new UDDINaming(uddiURL);

		log.info("Looking for '" + wsName + "'");
		String endpointAddress = uddiNaming.lookup(wsName);

		if (endpointAddress == null) {
			log.info("Not found!");
			return;
		} else {
			log.info("Found " + endpointAddress);
		}

		log.info("Creating stub ...");
		TransporterService service = new TransporterService();
		port = service.getTransporterPort();

		log.info("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
	}

	public TransporterPortType getPort() {
		return port;
	}

	public String getUddiURL() {
		return uddiURL;
	}

	public String getWsName() {
		return wsName;
	}
}
