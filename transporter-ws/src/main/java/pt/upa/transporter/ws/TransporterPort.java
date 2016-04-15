package pt.upa.transporter.ws;

import javax.jws.WebService;

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
public class TransporterPort implements TransporterPortType {
    static private final Logger log = LogManager.getRootLogger();
	
	private Manager manager = Manager.getInstance();

    @Override
    public String ping(String name) {
        log.debug("ping:");
        return "Pong " + name + "!";
    }

    @Override
    public JobView requestJob(String origin, String destination, int price)
            throws BadLocationFault_Exception, BadPriceFault_Exception {

        Job offerJob = manager.decideResponse(origin, destination, price);

        log.debug("requestJob:");
        if (offerJob != null) return offerJob.toJobView();

        return null;
    }

    @Override
    public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
        Job job = manager.confirmationJobs(id, accept);
        return job.toJobView();
    }

    @Override
    public JobView jobStatus(String id) {
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
        ArrayList<Job> jobs = manager.getJobs();

        log.debug("listJobs:");
        return jobListToJobViewList(jobs);
    }

    @Override
    public void clearJobs(){

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
}
