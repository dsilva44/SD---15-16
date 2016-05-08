package pt.upa.broker.domain;

import com.google.gson.Gson;
import pt.upa.broker.ws.EndpointManager;

public class BrokerBackup extends Broker {

    public BrokerBackup(String uddiURL, EndpointManager epm) {
        super(uddiURL, epm);
    }

    @Override
    public void updateTransport(Manager manager, String tSerialized) {
        if (tSerialized == null) {
            manager.clearTransports();
            manager.clearTransportersClients();
        } else {
            Transport transport = new Gson().fromJson(tSerialized, Transport.class);

            Transport oldT = manager.getTransportById(transport.getId());

            if (oldT == null) manager.addTransport(transport);
            else manager.replaceTransport(oldT, transport);
        }
    }


}
