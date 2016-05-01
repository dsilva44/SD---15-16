package pt.upa.ca.ws;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.ca.exception.CAException;

import javax.xml.registry.JAXRException;
import javax.xml.ws.Endpoint;
import java.util.regex.Pattern;

public class EndpointManager {
    static private final Logger log = LogManager.getRootLogger();

    private Endpoint endpoint = null;
    private UDDINaming uddiNaming = null;

    private String uddiURL;
    private String wsName = "UpaCA";
    private String wsURL;

    private boolean isStarted = false;
    private boolean isAwaitConnection = false;



    public EndpointManager(String uddiURL, String wsURL) throws CAException {
        String urlRegex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

        boolean validUddiURL = Pattern.matches(urlRegex, uddiURL);
        boolean validWsURL = Pattern.matches(urlRegex, wsURL);

        if (!validUddiURL) throw new IllegalArgumentException(uddiURL + " must be the form - http://host:port format!");
        else if (!validWsURL) throw new IllegalArgumentException(wsURL + " must be the form - http://host:port format!");

        this.uddiURL = uddiURL;
        this.wsURL = wsURL;

        endpoint = Endpoint.create(new CA());
        try {
            uddiNaming = new UDDINaming(uddiURL);
        } catch (JAXRException e) {
            throw new CAException("Cannot Create uddiNaming instance", e);
        }
    }

    public void start() throws CAException {
        try {
            // publish endpoint
            log.info("Starting: " + wsURL);
            endpoint.publish(wsURL);
        } catch (Exception e) {
            log.error("Error publish endpoint: " + wsURL, e);
            throw new CAException("Error publish endpoint: " + wsURL, e);
        }

        try {
            // publish to UDDI
            log.info("Publishing '"+ wsName + "' to UDDI at "+ uddiURL);
            uddiNaming.rebind(wsName, wsURL);
        } catch (Exception e) {
            log.error("Error on uddiNaming rebind", e);
            throw new CAException("Error on rebind", e);
        }
        isStarted = true;
    }

    public boolean awaitConnections() {
        return isAwaitConnection = isStarted;
    }

    public void stop() throws CAException {
        if(isAwaitConnection || isStarted) {
            try {
                if (endpoint != null) {
                    // stop endpoint
                    endpoint.stop();
                    log.info("Stopped " + wsURL);
                }
            } catch (Exception e) {
                log.error("Caught exception when stopping", e);
                throw new CAException("Fail to stop", e);
            }
            try {
                if (uddiNaming != null) {
                    // delete from UDDI
                    uddiNaming.unbind(wsName);
                    log.info("Deleted '"+ wsName +"' from UDDI");
                }
            } catch (Exception e) {
                log.error("Caught exception when deleting", e);
                throw new CAException("Fail to delete bind", e);
            }
        }
        isAwaitConnection = false;
        isStarted = false;
    }

    void setEndpoint(Endpoint endpoint) { this.endpoint = endpoint; }

    void setUddiNaming(UDDINaming uddiNaming) { this.uddiNaming = uddiNaming; }

    String getUddiURL() { return uddiURL; }

    Endpoint getEndpoint() { return endpoint; }

    String getWsName() { return wsName; }

    String getWsURL() { return wsURL; }

    boolean isStarted() { return isStarted; }

    boolean isAwaitConnection() { return isAwaitConnection; }

    void setStarted(boolean started) { isStarted = started; }

    void setAwaitConnection(boolean awaitConnection) { isAwaitConnection = awaitConnection; }
}
