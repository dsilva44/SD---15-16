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
        Manager manager = Manager.getInstance();

        BrokerPrimary brokerPrimary = new BrokerPrimary();
        manager.getEndPointManager().registerUddi();
        manager.setCurrBroker(brokerPrimary);
    }

    @Override
    public void monitor(long delay, long period) {
        EndpointManager epm = Manager.getInstance().getEndPointManager();

        BrokerPortType brokerPrimary = epm.createStub(epm.getWsURL2(), 2000, 2000);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {
                try {
                    brokerPrimary.ping("BrokerBackup");
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