package pt.upa.broker.ws;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.broker.domain.Manager;
import pt.upa.broker.domain.Transport;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;

import javax.jws.HandlerChain;
import javax.jws.WebService;


@WebService(
		endpointInterface = "pt.upa.broker.ws.BrokerPortType",
		wsdlLocation = "broker.1_0.wsdl",
		portName = "BrokerPort",
		targetNamespace = "http://ws.broker.upa.pt/",
		serviceName = "BrokerService"
)
@HandlerChain(file = "/handler-chain.xml")
public class BrokerPort implements BrokerPortType{
	static private final Logger log = LogManager.getRootLogger();

	private Manager manager = Manager.getInstance();
	
	@Override
	public String ping(String name) {
		int numResponses = manager.pingTransporters();

		log.debug("ping: " + numResponses);
		return numResponses + " transporters available";
	}

	@Override
	public String requestTransport(String origin, String destination, int price)
			throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {

		try {
			Transport transport = manager.requestTransport(origin, destination, price);
			manager.decideBestOffer(transport);

			log.debug("requestTransport: " + transport.getId() );
			return transport.getId();

		} catch (BadLocationFault_Exception e) {
			UnknownLocationFault faultInfo = new UnknownLocationFault();
			faultInfo.setLocation(e.getFaultInfo().getLocation());
			throw new UnknownLocationFault_Exception(e.getMessage(), faultInfo);
		} catch (BadPriceFault_Exception e) {
			InvalidPriceFault faultInfo = new InvalidPriceFault();
			faultInfo.setPrice(e.getFaultInfo().getPrice());
			throw new InvalidPriceFault_Exception(e.getMessage(), faultInfo);
		}
	}

	@Override
	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		Transport t = manager.updateTransportState(id);
		if (t != null) {
			log.debug("viewTransport return:" );
			return t.toTransportView();
		}
		log.debug("viewTransport return: " + null);
		UnknownTransportFault faultInfo = new UnknownTransportFault();
		faultInfo.setId(id);
		throw new UnknownTransportFault_Exception("Id unknown", faultInfo);
	}
	

	@Override
	public List<TransportView> listTransports() {
		ArrayList<Transport> transports = (ArrayList<Transport>) manager.getAllTransports();

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
	
}
