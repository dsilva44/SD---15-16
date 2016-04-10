package pt.upa.transporter.ws;

import javax.jws.WebService;

import pt.upa.transporter.domain.Manager;

import java.util.List;
@WebService(
        endpointInterface = "pt.upa.transporter.ws.TransporterPortType",
        wsdlLocation = "transporter.1_0.wsdl",
        portName = "TransporterPort",
        targetNamespace = "http://ws.transporter.upa.pt/",
        serviceName = "TransporterService"
)
public class TransporterPort implements TransporterPortType {
	
	Manager m = Manager.getInstance();
    @Override
    public String ping(String name) {
        return "Pong " + name + "!";
    }

    @Override
    public JobView requestJob(String origin, String destination, int price) throws BadLocationFault_Exception, BadPriceFault_Exception {
        //TODO requestJob
        return null;
    }

    @Override
    public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
        //TODO decideJob
        return null;
    }

    @Override
    public JobView jobStatus(String id) {
    	JobView job = m.getJobView(id);		//cannot change PortType prototype to throw exceptions
    	
    	if (job == null){
    		job = new JobView();
    	}
    	return job;
    }

    @Override
    public List<JobView> listJobs() {
        //TODO listJobs
        return null;
    }

    @Override
    public void clearJobs(){
    	m.setJobs(null);
    }
}
