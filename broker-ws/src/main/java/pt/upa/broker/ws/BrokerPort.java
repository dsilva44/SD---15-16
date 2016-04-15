package pt.upa.broker.ws;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pt.upa.broker.domain.Manager;
import pt.upa.broker.domain.Transport;
import pt.upa.broker.domain.TransportOffers;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;

import javax.jws.WebService;


@WebService(
		endpointInterface = "pt.upa.broker.ws.BrokerPortType",
		wsdlLocation = "broker.1_0.wsdl",
		portName = "BrokerPort",
		targetNamespace = "http://ws.broker.upa.pt/",
		serviceName = "BrokerService"
)
public class BrokerPort implements BrokerPortType{

	private Manager manager = Manager.getInstance();
	
	@Override
	public String ping(String name) {
		int numResponses = manager.pingTransporters();

		return numResponses + " transporters available";
	}

	@Override
	public String requestTransport(String origin, String destination, int price)
			throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {

		try {
			manager.requestTransport(origin, destination, price);
			return manager.decideOffers();

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
		
		if (t != null){
			return t.toTransportView();
		}
		return null;
	}
	

	@Override
	public List<TransportView> listTransports() {
		LinkedList<TransportOffers> transports = manager.getTransportOffers();
		
		return transportListToTransportViewList(transports);
	}

	@Override
	public void clearTransports() {
		//must remove transports in Transporter also
	}

	// TODO
	private List<TransportView> transportListToTransportViewList(LinkedList<TransportOffers> transports){
		ArrayList<TransportView> views = null;
		
		if (transports != null) {
            views = new ArrayList<>();
            for(TransportOffers transport : transports) {
                views.add(transport.getTransport().toTransportView());
            }
        }
		return views;
	}
	
}
