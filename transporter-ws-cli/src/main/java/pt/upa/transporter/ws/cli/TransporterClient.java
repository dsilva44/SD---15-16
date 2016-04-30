package pt.upa.transporter.ws.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.exception.TransporterClientException;
import pt.upa.transporter.ws.*;

import javax.xml.ws.BindingProvider;
import java.util.List;
import java.util.Map;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

public class TransporterClient implements TransporterPortType {
	static private final Logger log = LogManager.getRootLogger();

	private String wsName;
	private String uddiURL;
	private TransporterPortType port;
	private String wsURL;

	public TransporterClient(String uddiURL, String wsName) {
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		uddiLookup();
		createStub();
	}

	public TransporterClient(String wsURL) {
		this.wsURL = wsURL;
		createStub();
	}

	/** UDDI lookup */
	private void uddiLookup() throws TransporterClientException {
		try {
			log.info("Contacting UDDI at " + uddiURL);
			UDDINaming uddiNaming = new UDDINaming(uddiURL);

			log.info("Looking for '" + wsName + "'");
			wsURL = uddiNaming.lookup(wsName);

		} catch (Exception e) {
			String msg = String.format("Client failed lookup on UDDI at %s!", uddiURL);
			throw new TransporterClientException(msg, e);
		}

		if (wsURL == null) {
			String msg = String.format("Service with name %s not found on UDDI at %s", wsName, uddiURL);
			throw new TransporterClientException(msg);
		}
	}

	/** Stub creation and configuration */
	private void createStub() {
		log.info("Creating stub ...");
		TransporterService service = new TransporterService();
		port = service.getTransporterPort();

		log.info("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
	}

	/*-----------------------------------------------remote invocation methods----------------------------------------*/

	@Override
	public String ping(String name) {
		return port.ping(name);
	}

	@Override
	public JobView requestJob(String origin, String destination, int price)
			throws BadLocationFault_Exception, BadPriceFault_Exception {
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
