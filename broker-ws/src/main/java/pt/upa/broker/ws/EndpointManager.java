package pt.upa.broker.ws;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.exception.BrokerEndpointException;
import pt.upa.broker.exception.BrokerUddiNamingException;
import pt.upa.broker.domain.Manager;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

public class EndpointManager {
    static private final Logger log = LogManager.getRootLogger();

    private Endpoint endpoint = null;
    private UDDINaming uddiNaming = null;
    private String wsURL1;
    private String wsURL2;
    private String wsName;
    private String uddiURL;

    private boolean isPublish = false;
    private boolean isAwaitConnection = false;
    private boolean isRegister = false;

    public EndpointManager(String wsURL1, String wsURL2, String wsName, String uddiURL) {
        String urlRegex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        boolean validWsURL1 = Pattern.matches(urlRegex, wsURL1);
        boolean validWsURL2 = Pattern.matches(urlRegex, wsURL2);
        boolean validUddiURL = Pattern.matches(urlRegex, uddiURL);

        if(!validWsURL1 || !validWsURL2 || !validUddiURL)
            throw new IllegalArgumentException("Must be the form - http://host:port format!");

        this.wsURL1 = wsURL1;
        this.wsURL2 = wsURL2;
        this.wsName = wsName;
        this.uddiURL = uddiURL;

        try {
            uddiNaming = new UDDINaming(uddiURL);
        } catch (JAXRException e) {
            throw new BrokerUddiNamingException("Cannot Create uddiNaming instance");
        }

        endpoint = Endpoint.create(new BrokerPort());
    }


    public void start() {
        try {
            // publish endpoint
            log.info("Starting: " + wsURL1);
            endpoint.publish(wsURL1);
            createStub(wsURL1, 2000, 2000);
        } catch (Exception e) {
            log.error("Error publish endpoint: " + wsURL1, e);
            throw new BrokerEndpointException("Error publish endpoint: " + wsURL1);
        }
        isPublish = true;
    }

    public void registerUddi() {
        try {
            // publish to UDDI
            log.info("Publishing '"+ wsName + "' to UDDI at "+ uddiURL);
            uddiNaming.rebind(wsName, wsURL1);
        } catch (Exception e) {
            log.error("Error on uddiNaming rebind", e);
            throw new BrokerUddiNamingException("Error on rebind");
        }
        isRegister = true;
    }

    public Collection<String> findInUddi(String query) {
        Collection<String> result = null;

        try {
            result = uddiNaming.list(query);
        } catch (JAXRException e) {
            log.error("something goes wrong whit uddiNaming", e);
        }
        return result;
    }

    public boolean awaitConnections() {
        return isAwaitConnection = isPublish;
    }

    public void stop() {
        if(isAwaitConnection || isPublish) {
            try {
                if (endpoint.isPublished()) {
                    // stop endpoint
                    endpoint.stop();
                    log.info("Stopped " + wsURL1);
                }
            } catch (Exception e) {
                log.error("Caught exception when stopping", e);
                throw new BrokerEndpointException("Fail to stop");
            }
        }

        if (isRegister) {
            try {
                // delete from UDDI
                uddiNaming.unbind(wsName);
                log.info("Deleted '"+ wsName +"' from UDDI");
            } catch (Exception e) {
                log.error("Caught exception when deleting", e);
                throw new BrokerUddiNamingException("Fail to delete bind");
            }
        }
        isPublish = false;
        isRegister = false;
        isAwaitConnection = false;
    }

    /** Stub creation and configuration */
    public BrokerPortType createStub(String wsURL, int connTimeout , int recvTimeout) {
        BrokerService service = new BrokerService();
        BrokerPortType port = service.getBrokerPort();

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

    Endpoint getEndpoint() { return endpoint; }
    public String getWsURL1() { return wsURL1; }
    public String getWsURL2() { return wsURL2; }
    public String getWsName() { return wsName; }
    public String getUddiURL() { return uddiURL; }
    boolean isPublish() { return isPublish; }
    boolean isAwaitConnection() { return isAwaitConnection; }
    boolean isRegister() { return isRegister; }
    void setEndpoint(Endpoint endpoint) { this.endpoint = endpoint; }
    void setPublished(boolean published) { isPublish = published; }
    void setAwaitConnection(boolean awaitConnection) { isAwaitConnection = awaitConnection; }
}
