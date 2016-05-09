package pt.upa.transporter.ws.cli;

import example.ws.handler.AuthenticationHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.exception.TransporterClientException;
import pt.upa.transporter.ws.*;
//import pt.upa.broker.domain.Manager;


import javax.annotation.Resource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
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
			UDDINaming uddiNaming = new UDDINaming(uddiURL);
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
		TransporterService service = new TransporterService();
		port = service.getTransporterPort();

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
		setupMessageContext();
		return port.requestJob(origin, destination, price);
	}

	@Override
	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
		setupMessageContext();
		return port.decideJob(id, accept);
	}

	@Override
	public JobView jobStatus(String id) {
		setupMessageContext();
		return port.jobStatus(id);
	}

	@Override
	public List<JobView> listJobs() {
		setupMessageContext();
		return port.listJobs();
	}

	@Override
	public void clearJobs() {
		setupMessageContext();
		port.clearJobs();
	}

	private void setupMessageContext(){

		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(AuthenticationHandler.INVOKER_PROPERTY, "UpaBroker");
		requestContext.put(AuthenticationHandler.KSPATH_PROPERTY, "src/main/resources/UpaBroker.jks");
		requestContext.put(AuthenticationHandler.PASSWORD_PROPERTY, "pass"+"UpaBroker");

		//FIXME ENDPOINT_ADDRESS_PROPERTY
		//requestContext.put(ENDPOINT_ADDRESS_PROPERTY, "http://localhost:8080/broker-ws/endpoint");
	}

}
