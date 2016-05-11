package pt.upa.broker.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.broker.exception.BrokerException;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.EndpointManager;

import javax.xml.ws.WebServiceException;
import java.util.Timer;
import java.util.TimerTask;

public class BrokerBackup extends Broker {
    static private final Logger log = LogManager.getRootLogger();

    public BrokerBackup(String primaryURL) {
        super(primaryURL);
    }

    @Override
    public void addBackupURL(String url) {
        throw new BrokerException("BackUp Cannot add backups");
    }

    @Override
    public void goNext() {
        Manager manager = Manager.getInstance();

        BrokerPrimary brokerPrimary = new BrokerPrimary(getPrimaryURL());
        manager.getEndPointManager().registerUddi();
        manager.setCurrBroker(brokerPrimary);
    }

    @Override
    public void monitor(long delay, long period) {
        EndpointManager epm = Manager.getInstance().getEndPointManager();

        BrokerPortType brokerPrimary = epm.createStub(getPrimaryURL(), 2000, 2000);
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
