package pt.upa.broker.domain;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.EndpointManager;

import javax.xml.ws.WebServiceException;
import java.util.Timer;
import java.util.TimerTask;

public class BrokerBackup extends Broker {
    static private final Logger log = LogManager.getRootLogger();

    private final int connTimeout = 2000;
    private int recvPeriod = 2000;
    private BrokerPortType brokerPrimary;

    public BrokerBackup(String uddiURL, EndpointManager epm) {
        super(uddiURL, epm);
        brokerPrimary = createStub(connTimeout, recvPeriod);
        monitorPrimary();
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

    private void monitorPrimary() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {
                try {
                    brokerPrimary.ping("BrokerBackup");
                    log.debug("ping!!!");
                } catch (WebServiceException wse) {
                    log.error("BrokerPrimary is down: "+wse.getMessage());
                    registerUddi();
                    this.cancel();
                }
            }
        }, connTimeout, connTimeout);
    }
}
