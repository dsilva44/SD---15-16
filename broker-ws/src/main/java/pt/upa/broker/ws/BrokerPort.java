package pt.upa.broker.ws;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.broker.domain.Manager;
import pt.upa.broker.domain.Transport;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;

import javax.jws.WebService;
import javax.xml.ws.BindingProvider;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;


@WebService(
		endpointInterface = "pt.upa.broker.ws.BrokerPortType",
		wsdlLocation = "broker.2_0.wsdl",
		portName = "BrokerPort",
		targetNamespace = "http://ws.broker.upa.pt/",
		serviceName = "BrokerService"
)
public class BrokerPort implements BrokerPortType{
	static private final Logger log = LogManager.getRootLogger();

	private Manager manager = Manager.getInstance();
	
	@Override
	public String ping(String name) {
		int numResponses = manager.findTransporters();

		log.debug("ping: " + numResponses);
		return numResponses + " transporters available";
	}

	@Override
	public String requestTransport(String origin, String destination, int price)
			throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		Transport transport;

		try {
			transport = manager.requestTransport(origin, destination, price);
			manager.decideBestOffer(transport);

			if (updateBackup(transport) == null) {
				log.error("backup failed!!!");
				manager.throwUnavailableTransportFault(origin, destination);
			}

			log.debug("requestTransport: " + transport.getId() );
		} catch (BadLocationFault_Exception e) {
			manager.throwUnknownLocationFault(e.getMessage()); return null;
		} catch (BadPriceFault_Exception e) {
			manager.throwInvalidPriceFault(e.getFaultInfo().getPrice()); return null;
		}

		return transport.getId();
	}

	@Override
	public String updateTransport(String tSerialized) throws UnknownTransportFault_Exception  {

		log.debug("updateTransport:" );
		Transport transport = new Gson().fromJson( tSerialized, Transport.class );

		return "OK";
	}

	@Override
	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		Transport t = manager.updateTransportState(id);
		if (t == null) manager.throwUnknownTransportFault(id);

		log.debug("viewTransport return:" );

		assert t != null;
		return t.toTransportView();
	}
	

	@Override
	public List<TransportView> listTransports() {
		ArrayList<Transport> transports = (ArrayList<Transport>) manager.getTransportsList();

		log.debug("listTransports:");
		return transportListToTransportViewList(transports);
	}

	@Override
	public void clearTransports() {
		manager.clearTransports();
		manager.clearTransportersClients();
	}

	private List<TransportView> transportListToTransportViewList(ArrayList<Transport> transports){
		ArrayList<TransportView> views = null;
		
		if (transports != null) {
            views = new ArrayList<>();
            for(Transport transport : transports) {
                views.add(transport.toTransportView());
            }
        }

		log.debug("listTransports:");
		return views;
	}

	private String updateBackup(Transport transport) {
		EndpointManager epm = Manager.getInstance().getEndPointManager();
		BrokerPortType brokerBackup = createStub(epm.getWsURL2());

		String tSerialized = new Gson().toJson(transport);

		try {
			return brokerBackup.updateTransport(tSerialized);
		}
		catch (UnknownTransportFault_Exception e) {
			log.error("Transport does not exist", e);
			return null;
		}
	}

	/** Stub creation and configuration */
	private BrokerPortType createStub(String wsURL) {
		log.info("Creating stub ...");
		BrokerService service = new BrokerService();
		BrokerPortType port = service.getBrokerPort();

		log.info("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);

		return port;
	}
	
}
