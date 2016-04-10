package pt.upa.transporter.ws;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.exception.CannotCreateUddiNamingException;
import pt.upa.transporter.exception.InvalidTransporterNameException;

import javax.xml.registry.JAXRException;
import javax.xml.ws.Endpoint;
import java.util.regex.Pattern;

public class EndpointManager {
    private Endpoint endpoint = null;
    private UDDINaming uddiNaming = null;

    private String uddiURL;
    private String wsName;
    private String wsURL;

    private boolean isStarted;
    private boolean isAwaitConnection;



    public EndpointManager(String uddiURL, String wsName, String wsURL) {
        String urlRegex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        String upaTransporterNameRegex = "UpaTransporter[1-9][0-9]*";

        boolean validUddiURL = Pattern.matches(urlRegex, uddiURL);
        boolean validWsName = Pattern.matches(upaTransporterNameRegex, wsName);
        boolean validWsURL = Pattern.matches(urlRegex, wsURL);

        if (!validUddiURL) throw new IllegalArgumentException(uddiURL + " must be the form - http://host:port format!");
        else if (!validWsName) throw new InvalidTransporterNameException(wsName);
        else if (!validWsURL) throw new IllegalArgumentException(wsURL + " must be the form - http://host:port format!");

        this.uddiURL = uddiURL;
        this.wsName = wsName;
        this.wsURL = wsURL;

        endpoint = Endpoint.create(new TransporterPort());
        try {
            uddiNaming = new UDDINaming(uddiURL);
        } catch (JAXRException e) {
            throw new CannotCreateUddiNamingException();
        }
    }

    public void start() throws Exception {
        // TODO
    }

    public boolean awaitConnections() {
        return false;
        // TODO
    }

    public boolean stop() {
        return false;
        // TODO
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

    public void setAwaitConnection(boolean awaitConnection) { isAwaitConnection = awaitConnection; }
}
