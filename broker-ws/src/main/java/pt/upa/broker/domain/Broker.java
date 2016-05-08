package pt.upa.broker.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.exception.BrokerUddiNamingException;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.BrokerService;
import pt.upa.broker.ws.EndpointManager;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

public abstract class Broker {
    static private final Logger log = LogManager.getRootLogger();

    private String uddiURL;
    private EndpointManager epm;
    private UDDINaming uddiNaming;
    private boolean isRegister = false;

    public Broker(String uddiURL, EndpointManager epm) {
        String urlRegex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        boolean validUddiURL = Pattern.matches(urlRegex, uddiURL);
        if(!validUddiURL)
            throw new IllegalArgumentException("Must be the form - http://host:port format!");

        this.uddiURL = uddiURL;
        this.epm = epm;

        try {
            uddiNaming = new UDDINaming(uddiURL);
        } catch (JAXRException e) {
            throw new BrokerUddiNamingException("Cannot Create uddiNaming instance");
        }
    }

    String getUddiURL() {
        return uddiURL;
    }
    EndpointManager getEndPointManager() {
        return epm;
    }
    boolean isRegister() {
        return isRegister;
    }

    public String uddiNamingLookup(String name) {
        String result = null;
        try {
            result = uddiNaming.lookup(name);
        } catch (JAXRException e) {
            log.error("something goes wrong with uddiNaming", e);
        }
        return result;
    }

    public Collection<String> uddiNamingList(String query) {
        Collection<String> result = null;

        try {
            result = uddiNaming.list(query);
        } catch (JAXRException e) {
            log.error("something goes wrong with uddiNaming", e);
        }
        return result;
    }

    public void registerUddi() {
        try {
            // publish to UDDI
            log.info("Publishing '"+ epm.getWsName() + "' to UDDI at "+ uddiURL);
            uddiNaming.rebind(epm.getWsName(), epm.getWsURL1());
        } catch (JAXRException e) {
            throw new BrokerUddiNamingException("rebind error");
        }

        isRegister = true;
    }

    public void deleteFromUDDI() {
        try {
            if (isRegister) {
                // delete from UDDI
                uddiNaming.unbind(epm.getWsName());
                log.info("Deleted '"+ epm.getWsName() +"' from UDDI");
            }
        } catch (Exception e) {
            log.error("Caught exception when deleting", e);
            throw new BrokerUddiNamingException("Fail to delete bind");
        }
        isRegister = false;
    }

    /** Stub creation and configuration */
    public BrokerPortType createStub(int connTimeout , int recvTimeout) {
        BrokerService service = new BrokerService();
        BrokerPortType port = service.getBrokerPort();

        BindingProvider bindingProvider = (BindingProvider) port;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        requestContext.put(ENDPOINT_ADDRESS_PROPERTY, epm.getWsURL2());

        // The connection timeout property has different names in different versions of JAX-WS
        // Set them all to avoid compatibility issues
        final List<String> CONN_TIME_PROPS = new ArrayList<>();
        CONN_TIME_PROPS.add("com.sun.xml.ws.connect.timeout");
        CONN_TIME_PROPS.add("com.sun.xml.internal.ws.connect.timeout");
        CONN_TIME_PROPS.add("javax.xml.ws.client.connectionTimeout");
        // Set timeout until a connection is established (unit is milliseconds; 0 means infinite)
        for (String propName : CONN_TIME_PROPS)
            requestContext.put(propName, connTimeout);


        // The receive timeout property has alternative names
        // Again, set them all to avoid capability issues
        final List<String> RECV_TIMEOUT_PROPERTY = new ArrayList<>();
        RECV_TIMEOUT_PROPERTY.add("com.sun.xml.ws.request.timeout");
        RECV_TIMEOUT_PROPERTY.add("com.sun.xml.internal.ws.request.timeout");
        RECV_TIMEOUT_PROPERTY.add("javax.xml.ws.client.receiveTimeout");
        // Set timeout until the response is received (unit is milliseconds; 0 means infinite)
        for (String propertyName : RECV_TIMEOUT_PROPERTY)
            requestContext.put(propertyName, recvTimeout);

        return port;
    }

    public abstract void updateTransport(Manager manager, String tSerialized);
}
