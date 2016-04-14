package pt.upa.broker.ws.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.*;

import javax.xml.ws.BindingProvider;
import java.util.List;
import java.util.Map;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

public class BrokerClient implements BrokerPortType {
    protected static final Logger log = LogManager.getRootLogger();

    private BrokerPortType port;

    public BrokerClient(String uddiURL) throws Exception {
        log.info("Contacting UDDI at " + uddiURL);
        UDDINaming uddiNaming = new UDDINaming(uddiURL);

        log.info("Looking for 'UpaBroker'");
        String endpointAddress = uddiNaming.lookup("UpaBroker");

        if (endpointAddress == null) {
            log.info("Not found!");
            return;
        } else {
            log.info("Found " + endpointAddress);
        }

        log.info("Creating stub ...");
        BrokerService service = new BrokerService();
        port = service.getBrokerPort();

        log.info("Setting endpoint address ...");
        BindingProvider bindingProvider = (BindingProvider) port;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
    }

    @Override
    public String ping(String name) {
        return port.ping(name);
    }

    @Override
    public String requestTransport(String origin, String destination, int price) throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
        return port.requestTransport(origin, destination, price);
    }

    @Override
    public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
        return port.viewTransport(id);
    }

    @Override
    public List<TransportView> listTransports() {
        return port.listTransports();
    }

    @Override
    public void clearTransports() {
        port.clearTransports();

    }
}

