package pt.upa.broker.ws.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.exception.BrokerClientException;
import pt.upa.broker.ws.*;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

public class BrokerClient implements BrokerPortType {
    private static final Logger log = LogManager.getRootLogger();

    private final int CONN_TIME_OUT = 10000;
    private final int RECV_TIME_OUT = 10000;
    private final int NUM_TRIES = 3;

    private BrokerPortType port;
    private String wsURL;
    private String uddiURL;
    private String wsName;

    public BrokerClient(String uddiURL, String wsName)  {
        this.uddiURL = uddiURL;
        this.wsName = wsName;
        uddiLookup();
        createStub();
    }

    /** UDDI lookup */
    private void uddiLookup() {
        try {
            log.info("Contacting UDDI at " + uddiURL);
            UDDINaming uddiNaming = new UDDINaming(uddiURL);

            log.info("Looking for 'UpaBroker'");
            wsURL = uddiNaming.lookup(wsName);

        } catch (JAXRException e) {
            log.error("uddiNaming Error: " + e.getMessage());
        }
    }

    /** Stub creation and configuration */
    private void createStub() {
        log.info("Creating stub ...");
        BrokerService service = new BrokerService();
        port = service.getBrokerPort();

        if (wsURL != null) {
            log.info("Setting endpoint address ...");
            BindingProvider bindingProvider = (BindingProvider) port;
            Map<String, Object> requestContext = bindingProvider.getRequestContext();
            requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);

            // The connection timeout property has different names in different versions of JAX-WS
            // Set them all to avoid compatibility issues
            final List<String> CONN_TIME_PROPS = new ArrayList<>();
            CONN_TIME_PROPS.add("com.sun.xml.ws.connect.timeout");
            CONN_TIME_PROPS.add("com.sun.xml.internal.ws.connect.timeout");
            CONN_TIME_PROPS.add("javax.xml.ws.client.connectionTimeout");
            // Set timeout until a connection is established (unit is milliseconds; 0 means infinite)
            for (String propName : CONN_TIME_PROPS)
                requestContext.put(propName, CONN_TIME_OUT);


            // The receive timeout property has alternative names
            // Again, set them all to avoid capability issues
            final List<String> RECV_TIMEOUT_PROPERTY = new ArrayList<>();
            RECV_TIMEOUT_PROPERTY.add("com.sun.xml.ws.request.timeout");
            RECV_TIMEOUT_PROPERTY.add("com.sun.xml.internal.ws.request.timeout");
            RECV_TIMEOUT_PROPERTY.add("javax.xml.ws.client.receiveTimeout");
            // Set timeout until the response is received (unit is milliseconds; 0 means infinite)
            for (String propertyName : RECV_TIMEOUT_PROPERTY)
                requestContext.put(propertyName, RECV_TIME_OUT);
        }
    }

    /*-----------------------------------------------remote invocation methods----------------------------------------*/
    @Override
    public String ping(String name) {
        for(int i = NUM_TRIES; i > 0; i--) {
            try {
                return port.ping(name);
            } catch (WebServiceException wse) {
                log.error("ping: "+wse.getMessage());
                retry();
            }
        }
        throw new BrokerClientException("Cannot contact server!!!");
    }

    @Override
    public String requestTransport(String origin, String destination, int price)
            throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
            UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
        for(int i = NUM_TRIES; i > 0; i--) {
            try {
                return port.requestTransport(origin, destination, price);
            } catch (WebServiceException wse) {
                log.error("requestTransport: "+wse.getMessage());
                retry();
            }
        }
        throw new BrokerClientException("Cannot contact server!!!");
    }

    @Override
    public String updateTransport(String tSerialized) {
        for(int i = NUM_TRIES; i > 0; i--) {
            try {
                port.updateTransport(tSerialized);
            } catch (WebServiceException wse) {
                log.error("updateTransport: "+wse.getMessage());
                retry();
            }
        }
        throw new BrokerClientException("Cannot contact server!!!");
    }

    @Override
    public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
        for(int i = NUM_TRIES; i > 0; i--) {
            try {
                return port.viewTransport(id);
            } catch (WebServiceException wse) {
                log.error("viewTransport: "+wse.getMessage());
                retry();
            }
        }
        throw new BrokerClientException("Cannot contact server!!!");
    }

    @Override
    public List<TransportView> listTransports() {
        for(int i = NUM_TRIES; i > 0; i--) {
            try {
                return port.listTransports();
            } catch (WebServiceException wse) {
                log.error("listTransports: "+wse.getMessage());
                retry();
            }
        }
        throw new BrokerClientException("Cannot contact server!!!");
    }

    @Override
    public void clearTransports() {
        for(int i = NUM_TRIES; i > 0; i--) {
            try {
                port.clearTransports();
                return;
            } catch (WebServiceException wse) {
                log.error("listTransports: "+wse.getMessage());
                retry();
            }
        }
        throw new BrokerClientException("Cannot contact server!!!");
    }

    private void retry() {
        uddiLookup();
        createStub();
    }

    private void nap(int seconds) {
        try {
            Thread.sleep(seconds*1000);
        } catch(InterruptedException e) {
            log.error(e);
        }
    }
}

