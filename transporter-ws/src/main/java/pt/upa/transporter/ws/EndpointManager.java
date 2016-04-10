package pt.upa.transporter.ws;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import javax.xml.ws.Endpoint;

public class EndpointManager {
    private Endpoint endpoint;
    private UDDINaming uddiNaming;

    private String uddiURL;
    private String wsName;
    private String wsURL;

    private boolean isStarted;
    private boolean isAwaitConnection;



    public EndpointManager(String uddiURL, String wsName, String wsURL) {
        // TODO
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

    String getWsName() { return wsName; }

    String getWsURL() { return wsURL; }

    boolean isStarted() { return isStarted; }

    boolean isAwaitConnection() { return isAwaitConnection; }

    void setStarted(boolean started) { isStarted = started; }

    public void setAwaitConnection(boolean awaitConnection) { isAwaitConnection = awaitConnection; }
}
