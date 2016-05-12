package pt.upa.transporter.ws;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import example.ws.handler.AuthenticationHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.transporter.domain.Job;
import pt.upa.transporter.domain.Manager;

import java.util.ArrayList;
import java.util.List;
@WebService(
        endpointInterface = "pt.upa.transporter.ws.TransporterPortType",
        wsdlLocation = "transporter.1_0.wsdl",
        portName = "TransporterPort",
        targetNamespace = "http://ws.transporter.upa.pt/",
        serviceName = "TransporterService"
)
@HandlerChain(file = "/transporter_handler-chain.xml")
public class TransporterPort implements TransporterPortType {
    static private final Logger log = LogManager.getRootLogger();
    private Manager manager = Manager.getInstance();

    @Resource
    private WebServiceContext wsContext;

    @Override
    public String ping(String name) {
        setupMessageContext();
        log.debug("ping:");
        return "Pong " + name + "!";
    }

    @Override
    public JobView requestJob(String origin, String destination, int price)
            throws BadLocationFault_Exception, BadPriceFault_Exception {

        setupMessageContext();
        Job offerJob = manager.decideResponse(origin, destination, price);
        log.debug("requestJob:");
        if (offerJob != null) return offerJob.toJobView();

        return null;
    }

    @Override
    public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {

        setupMessageContext();
        Job job = manager.confirmationJobs(id, accept);
        log.debug("decideJob:");

        return job.toJobView();
    }

    @Override
    public JobView jobStatus(String id) {

        setupMessageContext();
        Job job = manager.getJobById(id);

    	if (job==null) {
            log.debug("jobStatus:");
    		return null;
    	}
    	else{
            log.debug("jobStatus:");
    		return job.toJobView();
    	}
    }

    @Override
    public List<JobView> listJobs() {
        setupMessageContext();
        ArrayList<Job> jobs = manager.getJobs();
        log.debug("listJobs:");

        return jobListToJobViewList(jobs);
    }

    @Override
    public void clearJobs(){

        setupMessageContext();
        log.debug("clearJobs:");
        manager.setJobs(null);
    }

    private List<JobView> jobListToJobViewList(ArrayList<Job> jobs) {
        ArrayList<JobView> newList = null;

        if (jobs != null) {
            newList = new ArrayList<>();
            for(Job job : jobs) {
                newList.add(job.toJobView());
            }
        }

        log.debug("jobListToJobViewList:");
        return newList;
    }

    private void setupMessageContext(){
        String companyName = manager.getTransporterName();
        String path = manager.getKeyStorePath();
        String pass = manager.getPassword();
        MessageContext messageContext = wsContext.getMessageContext();
        messageContext.put(AuthenticationHandler.INVOKER_PROPERTY, companyName);
        messageContext.put(AuthenticationHandler.KSPATH_PROPERTY, path);
        messageContext.put(AuthenticationHandler.PASSWORD_PROPERTY, pass);
    }

    void setWsContext(WebServiceContext wsContext){
        this.wsContext = wsContext;
    }






}
