package pt.upa.broker.ws;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.broker.domain.Manager;
import pt.upa.broker.domain.Transport;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;

import javax.jws.WebService;
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

			if (update(transport) == null) {
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
	public String updateTransport(String tSerialized) {
		log.debug("updateTransport:" );

		Transport transport = new Gson().fromJson(tSerialized, Transport.class );
		manager.updateTransport(transport);

		return "OK";
	}

	@Override
	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		Transport t = manager.updateTransportState(id);
		if (t == null) manager.throwUnknownTransportFault(id);

		log.debug("viewTransport return:" );

		if (update(t) == null) {
			log.error("backup failed!!!");
			manager.throwUnknownTransportFault(id);
		}

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

	private String update(Transport transport) {
		BrokerPortType brokerBackup = manager.getBroker().createStub();
		String tSerialized = new Gson().toJson(transport);
		return brokerBackup.updateTransport(tSerialized);
	}
}
