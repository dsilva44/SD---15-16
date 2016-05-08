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
    private String wsURL1;
    private String wsURL2;

    private boolean isPublish = false;
    private boolean isAwaitConnection = false;



    public EndpointManager(String wsURL1, String wsURL2, String wsName) {
        String urlRegex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

        boolean validWsURL1 = Pattern.matches(urlRegex, wsURL1);
        boolean validWsURL2 = Pattern.matches(urlRegex, wsURL2);

        if(!validWsURL1 || !validWsURL2)
            throw new IllegalArgumentException("Must be the form - http://host:port format!");

        this.wsURL1 = wsURL1;
        this.wsURL2 = wsURL2;
        this.wsName = wsName;

        endpoint = Endpoint.create(new BrokerPort());
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
        isPublish = false;
        isAwaitConnection = false;
    }

    void setEndpoint(Endpoint endpoint) { this.endpoint = endpoint; }

    Endpoint getEndpoint() { return endpoint; }

    public String getWsName() { return wsName; }

    public String getWsURL1() { return wsURL1; }
    public String getWsURL2() { return wsURL2; }

    boolean isPublish() { return isPublish; }

    boolean isAwaitConnection() { return isAwaitConnection; }

    void setPublished(boolean published) { isPublish = published; }

    void setAwaitConnection(boolean awaitConnection) { isAwaitConnection = awaitConnection; }
}
