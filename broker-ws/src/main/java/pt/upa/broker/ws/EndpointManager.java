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
    private String wsName;
    private String wsURL;

    private boolean isPublish = false;
    private boolean isAwaitConnection = false;



    public EndpointManager(String wsURL, String wsName) {
        String urlRegex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

        boolean validWsURL = Pattern.matches(urlRegex, wsURL);

        if(!validWsURL)
            throw new IllegalArgumentException("Must be the form - http://host:port format!");

        this.wsURL = wsURL;
        this.wsName = wsName;

        endpoint = Endpoint.create(new BrokerPort());
    }



    public void start() {
        try {
            // publish endpoint
            log.info("Starting: " + wsURL);
            endpoint.publish(wsURL);
        } catch (Exception e) {
            log.error("Error publish endpoint: " + wsURL, e);
            throw new BrokerEndpointException("Error publish endpoint: " + wsURL);
        }
        isPublish = true;
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
                    log.info("Stopped " + wsURL);
                }
            } catch (Exception e) {
                log.error("Caught exception when stopping", e);
                throw new BrokerEndpointException("Fail to stop");
            }
        }
        isPublish = false;
        isAwaitConnection = false;
    }

    void setEndpoint(Endpoint endpoint) { this.endpoint = endpoint; }

    Endpoint getEndpoint() { return endpoint; }

    public String getWsName() { return wsName; }

    public String getWsURL() { return wsURL; }

    boolean isPublish() { return isPublish; }

    boolean isAwaitConnection() { return isAwaitConnection; }

    void setPublished(boolean published) { isPublish = published; }

    void setAwaitConnection(boolean awaitConnection) { isAwaitConnection = awaitConnection; }
}
