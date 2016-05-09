package pt.upa.broker.ws;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.broker.domain.Manager;
import pt.upa.broker.domain.Transport;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;

import javax.jws.WebService;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.List;


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
		return "pong "+name+"!!!";
	}

	@Override
	public String requestTransport(String origin, String destination, int price)
			throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		Transport transport;

		try {
			transport = manager.requestTransport(origin, destination, price);
			manager.decideBestOffer(transport);

			updateBackup(transport);

		} catch (BadLocationFault_Exception e) {
			manager.throwUnknownLocationFault(e.getMessage()); return null;
		} catch (BadPriceFault_Exception e) {
			manager.throwInvalidPriceFault(e.getFaultInfo().getPrice()); return null;
		}

		return transport.getId();
	}

	@Override
	public String updateTransport(String tSerialized) {
		manager.getCurrBroker().updateTransport(tSerialized);

		return "OK";
	}

	@Override
	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		Transport transport = manager.updateTransportState(id);
		if (transport == null) manager.throwUnknownTransportFault(id);

		updateBackup(transport);

		return transport.toTransportView();
	}


	@Override
	public List<TransportView> listTransports() {
		ArrayList<Transport> transports = (ArrayList<Transport>) manager.getTransportsList();
		return transportListToTransportViewList(transports);
	}

	@Override
	public void clearTransports() {
		manager.clearTransports();
		manager.clearTransportersClients();

		updateBackup(null);
	}

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

	private String updateBackup(Transport transport) {
		String tSerialized = null;
		if (transport != null) {
			log.debug("Backup: "+transport.toString());
			tSerialized = new Gson().toJson(transport);
		}
		try {
			EndpointManager epm = manager.getEndPointManager();
			BrokerPortType brokerStub = epm.createStub(epm.getWsURL2(), 2000, 2000);
			return brokerStub.updateTransport(tSerialized);
		} catch (WebServiceException wse) {
			return null;
		}
	}
}
