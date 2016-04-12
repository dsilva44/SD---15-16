package pt.upa.broker.ws;

import java.util.List;

import pt.upa.broker.domain.Manager;
import pt.upa.broker.domain.Transport;

public class BrokerPort implements BrokerPortType{

	private Manager manager = Manager.getInstance();
	
	@Override
	public String ping(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String requestTransport(String origin, String destination, int price)
			throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TransportView> listTransports() {
		/*List<Transport> transports = manager.listTransports();
		List <TransportView> transportViews = new List<TransportView>();
		
		for (Transport t : transports){
			transportViews.add(t.toTransportView());
		}
		return transportViews;*/
		return null;
	}

	@Override
	public void clearTransports() {
		//must remove transports in Transporter also
	}

	// TODO

}
