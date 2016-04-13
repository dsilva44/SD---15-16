package pt.upa.transporter.ws;

import javax.jws.WebService;

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
	
	private Manager manager = Manager.getInstance();

    @Override
    public String ping(String name) {
        return "Pong " + name + "!";
    }

    @Override
    public JobView requestJob(String origin, String destination, int price)
            throws BadLocationFault_Exception, BadPriceFault_Exception {
        manager.validateRequestedJob(origin, destination, price);
        Job offerJob = manager.decideResponse(origin, destination, price);

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

    	if (job==null){
    		return null;
    	}
    	else{
    		return job.toJobView();
    	}
    }

    @Override
    public List<JobView> listJobs() {
        ArrayList<Job> jobs = manager.getJobs();
        return jobListToJobViewList(jobs);
    }

    @Override
    public void clearJobs(){
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

        return newList;
    }
}
