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

    private final int CONN_TIMEOUT = 2000;
    private int RECV_TIMEOUT = 2000;
    private BrokerPortType brokerPrimary;

    public BrokerBackup(String uddiURL, EndpointManager epm) {
        super(uddiURL, epm);
        brokerPrimary = createStub(CONN_TIMEOUT, RECV_TIMEOUT);
        monitorPrimary();
    }

    @Override
    public void updateTransport(Manager manager, String tSerialized) {
        if (tSerialized == null) {
            manager.clearTransports();
            manager.clearTransportersClients();
            log.debug("Cleaning...");
        } else {
            Transport transport = new Gson().fromJson(tSerialized, Transport.class);

            Transport oldT = manager.getTransportById(transport.getId());

            if (oldT == null) {
                manager.addTransport(transport);
                log.debug("Create: "+transport.toString());
            }
            else {
                manager.replaceTransport(oldT, transport);
                log.debug("Update: "+transport.toString());
            }
        }
    }

    private void monitorPrimary() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {
                try {
                    brokerPrimary.ping("BrokerBackup");
                    log.debug("--------------Is Alive----------------");
                } catch (WebServiceException wse) {
                    //Substitute primary
                    log.error("BrokerPrimary is down: "+wse.getMessage());
                    registerUddi();
                    this.cancel();
                }
            }
        }, CONN_TIMEOUT, CONN_TIMEOUT);
    }

    private void replacePrimary() {

    }
}
