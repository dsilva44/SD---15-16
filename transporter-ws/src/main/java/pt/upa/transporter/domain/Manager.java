package pt.upa.transporter.domain;

import pt.upa.transporter.exception.JobDoesNotExistException;
import pt.upa.transporter.exception.WrongStateToConfirmException;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Manager {
    private static Manager manager = new Manager();

    private String parity;
    private ArrayList<String> workCities;
    private ArrayList<JobView> jobs;

    private Manager() {}

    public static Manager getInstance() { return manager; }

    public void init(String transporterName) {
        int tNum = Integer.parseInt(transporterName.substring(transporterName.length() - 1));
        if (tNum % 2 == 0) { parity = "EVEN"; }
        else parity = "ODD";

        ArrayList<String> centro = new ArrayList<>(Arrays.asList("Lisboa", "Leiria", "Santarém", "Castelo Branco",
                                                                    "Coimbra", "Aveiro", "Viseu", "Guarda"));
        ArrayList<String> norte = new ArrayList<>(Arrays.asList("Porto", "Braga", "Viana do Castelo", "Vila Real",
                                                                    "Bragança"));
        ArrayList<String> sul = new ArrayList<>(Arrays.asList("Setúbal", "Évora", "Portalegre", "Beja", "Faro"));

        switch (parity) {
            case "EVEN":
                centro.addAll(norte);
                workCities = new ArrayList<>(centro);
            case "ODD":
                centro.addAll(sul);
                workCities = new ArrayList<>(centro);
        }

        jobs = new ArrayList<JobView>();
    }

    public String getParity() {
        return parity;
    }

    public ArrayList<JobView> getJobs() {
        return jobs;
    }


    public List<String> getWorkCities() { return workCities; }

    public boolean decideResponde() {
        //TODO RequestJob
        return false;
    }

    public void TransportSimulation() {
        // TODO
    }

    public JobView confirmationJobs(String id, boolean bool) throws Exception{

        JobView job = getJobView(id);
        if(job == null){
            throw new JobDoesNotExistException(id);
        }

        if (job.getJobState() != JobStateView.PROPOSED) throw new WrongStateToConfirmException();

        if (bool){
            job.setJobState(JobStateView.ACCEPTED);
        }
        else{
            job.setJobState(JobStateView.REJECTED);
        }

        return job;
    }

    public void addJob(JobView jobtoadd){
        jobs.add(jobtoadd);
    }

    public void removeJob(JobView jobtoremove){
        jobs.remove(jobtoremove);
    }

    public void setJobs(ArrayList<JobView> list){ 
    	if (list == null){
    		jobs.clear();
    	}
    	else{
    		jobs = list;
    	}
    }

    public JobView getJobView(String id){
        for (JobView job:jobs){
            if (job.getJobIdentifier().equals(id)){
                return job;
            }
        }
        return null;
    }
}
