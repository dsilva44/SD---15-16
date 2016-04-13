package pt.upa.broker.ws;

import java.util.ArrayList;
import java.util.List;

import pt.upa.broker.domain.Manager;
import pt.upa.broker.domain.Transport;
import pt.upa.transporter.domain.Job;

public class BrokerPort implements BrokerPortType{

	private Manager manager = Manager.getInstance();
	
	@Override
	public String ping(String name) {
		//TODO
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
		ArrayList<Transport> transports = manager.getBookedTransports();
		
		return transportListToTransportViewList(transports);
	}

	@Override
	public void clearTransports() {
		//must remove transports in Transporter also
	}

	// TODO
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
	
}
