package pt.upa.broker.ws;

import example.ws.handler.RepeatedMessageServerHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.broker.domain.Manager;
import pt.upa.broker.domain.Transport;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@WebService(
		endpointInterface = "pt.upa.broker.ws.BrokerPortType",
		wsdlLocation = "broker.2_0.wsdl",
		portName = "BrokerPort",
		targetNamespace = "http://ws.broker.upa.pt/",
		serviceName = "BrokerService"
)
@HandlerChain(file = "/broker_handler-chain.xml")
public class BrokerPort implements BrokerPortType{
	static private final Logger log = LogManager.getRootLogger();

	@Resource
	private WebServiceContext webServiceContext;

	private Manager manager = Manager.getInstance();
	
	@Override
	public String ping(String name) {
		log.debug("oprID = "+getOprID());

		return "pong "+name+"!!!";
	}

	@Override
	public String requestTransport(String origin, String destination, int price)
			throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		log.debug("oprID = "+getOprID());

		Transport transport;
		try {
			transport = manager.requestTransport(origin, destination, price);
			manager.decideBestOffer(transport);

			updateBackup(transport.toTransportView(), transport.getChosenOfferID());

		} catch (BadLocationFault_Exception e) {
			manager.throwUnknownLocationFault(e.getMessage()); return null;
		} catch (BadPriceFault_Exception e) {
			manager.throwInvalidPriceFault(e.getFaultInfo().getPrice()); return null;
		}

		return transport.getId();
	}

	@Override
	public String registerBackup(String wsURL) {
		//TODO - Throw exception if invalid wsURL
		manager.getCurrBroker().addBackupURL(wsURL);

		return "OK";
	}

	@Override
	public String updateTransport(TransportView transportView, String bestOfferID) {
		manager.updateTransport(transportView, bestOfferID);

		return "OK";
	}

	@Override
	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		log.debug("oprID = "+getOprID());

		Transport transport = manager.updateTransportState(id);
		if (transport == null) manager.throwUnknownTransportFault(id);

		updateBackup(transport.toTransportView(), transport.getChosenOfferID());

		return transport.toTransportView();
	}


	@Override
	public List<TransportView> listTransports() {
		log.debug("oprID = "+getOprID());

		ArrayList<Transport> transports = (ArrayList<Transport>) manager.getTransportsList();
		return transportListToTransportViewList(transports);
	}

	@Override
	public void clearTransports() {
		log.debug("oprID = "+getOprID());

		manager.clearTransports();
		manager.clearTransportersClients();

		clearBackups();
	}

	//-------------------------------------------Aux methods------------------------------------------------------------

	private List<TransportView> transportListToTransportViewList(ArrayList<Transport> transports){
		ArrayList<TransportView> views = null;

		if (transports != null) {
			views = new ArrayList<>();
			for(Transport transport : transports) {
				views.add(transport.toTransportView());
			}
		}
		return views;
	}

	private void updateBackup(TransportView transportView, String chosenID) {
		Transport transport = new Transport(transportView, chosenID); // just for debug
		log.debug("Backup: "+transport.toString());

		EndpointManager endpointManager = manager.getEndPointManager();
		Iterator<String> iterator = manager.getCurrBroker().getBackupURLs().iterator();
		while(iterator.hasNext()) {
			try {
				BrokerPortType brokerStub = endpointManager.createStub(iterator.next(), 2000, 2000);
				brokerStub.updateTransport(transportView, chosenID);
			} catch (WebServiceException e) {
				log.error("Backup endpoint down");
				iterator.remove();
			}
		}
	}

	private void clearBackups() {
		log.debug("Clear...");

		EndpointManager endpointManager = manager.getEndPointManager();
		Iterator<String> iterator = manager.getCurrBroker().getBackupURLs().iterator();
		while(iterator.hasNext()) {
			try {
				BrokerPortType brokerStub = endpointManager.createStub(iterator.next(), 2000, 2000);
				brokerStub.clearTransports();
			} catch (WebServiceException e) {
				log.error("Backup endpoint down");
				iterator.remove();
			}
		}
	}

	private String getOprID() {
		MessageContext messageContext = webServiceContext.getMessageContext();

		return (String) messageContext.get(RepeatedMessageServerHandler.OPR_ID_PROPERTY);
	}
}
