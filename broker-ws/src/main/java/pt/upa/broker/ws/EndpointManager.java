package pt.upa.broker.ws;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.exception.BrokerEndpointException;
import pt.upa.broker.exception.BrokerUddiNamingException;
import pt.upa.broker.domain.Manager;

import javax.xml.registry.JAXRException;
import javax.xml.ws.Endpoint;
import java.util.Collection;
import java.util.regex.Pattern;

public class EndpointManager {
    static private final Logger log = LogManager.getRootLogger();

    private Endpoint endpoint = null;
    private UDDINaming uddiNaming = null;

    private String uddiURL;
    private String wsName;
    private String wsURL1;
    private String wsURL2;

    private boolean isPublish = false;
    private boolean isAwaitConnection = false;
    private boolean isRegister = false;



    public EndpointManager(String wsURL1, String wsURL2, String wsName, String uddiURL) {
        String urlRegex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

        boolean validUddiURL = Pattern.matches(urlRegex, uddiURL);
        boolean validWsURL1 = Pattern.matches(urlRegex, wsURL1);
        boolean validWsURL2 = Pattern.matches(urlRegex, wsURL2);

        if(!validWsURL1 || !validWsURL2 || !validUddiURL)
            throw new IllegalArgumentException("Must be the form - http://host:port format!");

        this.wsURL1 = wsURL1;
        this.wsURL2 = wsURL2;
        this.wsName = wsName;
        this.uddiURL = uddiURL;

        endpoint = Endpoint.create(new BrokerPort());

        try {
            uddiNaming = new UDDINaming(uddiURL);
        } catch (JAXRException e) {
            throw new BrokerUddiNamingException("Cannot Create uddiNaming instance");
        }

        Manager.getInstance().init(this);
    }



    public void start() {
        try {
            // publish endpoint
            log.info("Starting: " + wsURL1);
            endpoint.publish(wsURL1);
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

    public Collection<String> uddiNamingList(String query) {
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
            try {
                if (isRegister) {
                    // delete from UDDI
                    uddiNaming.unbind(wsName);
                    log.info("Deleted '"+ wsName +"' from UDDI");
                }
            } catch (Exception e) {
                log.error("Caught exception when deleting", e);
                throw new BrokerUddiNamingException("Fail to delete bind");
            }
        }
        isPublish = false;
        isRegister = false;
        isAwaitConnection = false;
    }

    void setEndpoint(Endpoint endpoint) { this.endpoint = endpoint; }

    void setUddiNaming(UDDINaming uddiNaming) { this.uddiNaming = uddiNaming; }

    public String getUddiURL() { return uddiURL; }

    Endpoint getEndpoint() { return endpoint; }

    String getWsName() { return wsName; }

    String getWsURL1() { return wsURL1; }
    public String getWsURL2() { return wsURL2; }

    boolean isPublish() { return isPublish; }

    boolean isAwaitConnection() { return isAwaitConnection; }

    void setPublished(boolean published) { isPublish = published; }

    void setRegister(boolean register) {
        isRegister = register;
    }

    void setAwaitConnection(boolean awaitConnection) { isAwaitConnection = awaitConnection; }
}
