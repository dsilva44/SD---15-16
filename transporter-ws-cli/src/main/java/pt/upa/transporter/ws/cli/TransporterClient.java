package pt.upa.transporter.ws.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.*;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import java.util.List;
import java.util.Map;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

public class TransporterClient implements TransporterPortType {
	static private final Logger log = LogManager.getRootLogger();

	private TransporterPortType port;
	private String wsURL;

	public TransporterClient(String uddiURL, String wsName) throws JAXRException {
		log.info("Contacting UDDI at " + uddiURL);
		UDDINaming uddiNaming = new UDDINaming(uddiURL);

		log.info("Looking for '" + wsName + "'");
		String endpointAddress = uddiNaming.lookup(wsName);

		if (endpointAddress == null) {
			log.info("Not found!");
			return;
		} else {
			log.info("Found " + endpointAddress);
			this.wsURL = endpointAddress;
		}

		log.info("Creating stub ...");
		TransporterService service = new TransporterService();
		port = service.getTransporterPort();

		log.info("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
	}

	public TransporterClient(String wsURL) {
		this.wsURL = wsURL;
		log.info("Creating stub ...");
		TransporterService service = new TransporterService();
		port = service.getTransporterPort();

		log.info("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
	}

	public String getWsURL() { return wsURL; }

	@Override
	public String ping(String name) {
		return port.ping(name);
	}

	@Override
	public JobView requestJob(String origin, String destination, int price) throws BadLocationFault_Exception, BadPriceFault_Exception {
		return port.requestJob(origin, destination, price);
	}

	@Override
	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
		return port.decideJob(id, accept);
	}

	@Override
	public JobView jobStatus(String id) {
		return port.jobStatus(id);
	}

	@Override
	public List<JobView> listJobs() {
		return port.listJobs();
	}

	@Override
	public void clearJobs() {
		port.clearJobs();
	}
}
