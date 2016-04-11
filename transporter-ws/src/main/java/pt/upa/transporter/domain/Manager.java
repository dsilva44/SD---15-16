package pt.upa.transporter.domain;

import pt.upa.transporter.exception.JobDoesNotExistException;
import pt.upa.transporter.exception.WrongStateToConfirmException;
import pt.upa.transporter.ws.JobStateView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Manager {
    private static Manager manager = new Manager();

    private String parity;
    private ArrayList<String> workCities;
    private ArrayList<Job> jobs;

    private Manager() {
        workCities = new ArrayList<>();
        jobs = new ArrayList<>();
    }

    public static Manager getInstance() { return manager; }

    public void init(String transporterName) {
        String upaTransporterNameRegex = "UpaTransporter[1-9][0-9]*";
        boolean validTransporterName = Pattern.matches(upaTransporterNameRegex, transporterName);
        if (!validTransporterName) throw new IllegalArgumentException(transporterName);

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
                break;
            case "ODD":
                centro.addAll(sul);
                workCities = new ArrayList<>(centro);
                break;
        }
    }

    String getParity() { return parity; }

    ArrayList<String> getWorkCities() { return workCities; }

    ArrayList<Job> getJobs() {
        return jobs;
    }

    public boolean decideResponde() {
        //TODO RequestJob
        return false;
    }

    public void TransportSimulation() {
        // TODO
    }

    public Job confirmationJobs(String id, boolean bool) {

        Job job = getJobById(id);
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

    void addJob(Job job){
        jobs.add(job);
    }

    void removeJob(Job job){
        jobs.remove(job);
    }

    public void setJobs(ArrayList<Job> list){
    	if (list == null){
    		jobs.clear();
    	}
    	else{
    		jobs = list;
    	}
    }

    public Job getJobById(String id){
        for (Job job:jobs){
            if (job.getJobIdentifier().equals(id)){
                return job;
            }
        }
        return null;
    }
}
