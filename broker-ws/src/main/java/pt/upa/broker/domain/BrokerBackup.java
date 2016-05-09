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

    public BrokerBackup(String uddiURL, EndpointManager epm) {
        super(uddiURL, epm);
    }

    @Override
    public void updateTransport(String tSerialized) {
        Manager manager = Manager.getInstance();

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

    @Override
    public void goNext() {
        BrokerPrimary brokerPrimary = new BrokerPrimary(getUddiURL(), getEndPointManager());
        brokerPrimary.registerUddi();
        Manager.getInstance().setCurrBroker(brokerPrimary);
    }

    @Override
    public void monitor(long delay, long period) {
        BrokerPortType broker = createStub(2000, 2000);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {
                try {
                    broker.ping("BrokerBackup");
                    log.debug("--------------Is Alive----------------");
                } catch (WebServiceException wse) {
                    log.debug("--------------Is Dead.----------------");
                    Manager.getInstance().goNext();
                    this.cancel();
                }
            }
        }, delay, period);
    }

}
