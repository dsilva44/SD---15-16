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
    private ArrayList<Job> jobs;

    private final ArrayList<String> knowCities;
    private ArrayList<String> workCities;
    private final ArrayList<String> centro = new ArrayList<>(Arrays.asList("Lisboa", "Leiria", "Santarém",
            "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda"));
    private final ArrayList<String> norte = new ArrayList<>(Arrays.asList("Porto", "Braga", "Viana do Castelo",
            "Vila Real", "Bragança"));
    private final ArrayList<String> sul = new ArrayList<>(Arrays.asList("Setúbal", "Évora", "Portalegre", "Beja",
            "Faro"));

    private Manager() {
        knowCities = new ArrayList<>();
        workCities = new ArrayList<>();
        jobs = new ArrayList<>();

        knowCities.addAll(centro);
        knowCities.addAll(norte);
        knowCities.addAll(sul);
    }

    public static Manager getInstance() { return manager; }

    public void init(String transporterName) {
        String upaTransporterNameRegex = "UpaTransporter[1-9][0-9]*";
        boolean validTransporterName = Pattern.matches(upaTransporterNameRegex, transporterName);
        if (!validTransporterName) throw new IllegalArgumentException(transporterName);

        int tNum = Integer.parseInt(transporterName.substring(transporterName.length() - 1));
        if (tNum % 2 == 0) { parity = "EVEN"; }
        else parity = "ODD";

        workCities.addAll(centro);

        switch (parity) {
            case "EVEN":
                workCities.addAll(norte);
                break;
            case "ODD":
                workCities.addAll(sul);
                break;
        }

    }

    String getParity() { return parity; }

    ArrayList<String> getWorkCities() { return workCities; }

    public ArrayList<Job> getJobs() {
        return jobs;
    }

    public Job decideResponse(String origin, String destination, int price) {
        //TODO
        return new Job();
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

    public void setJobs(ArrayList<Job> jobs){
    	if (jobs == null) this.jobs.clear();
    	else this.jobs = jobs;
    }

    void setWorkCities(ArrayList<String> workCities) {
        if (workCities == null) this.workCities.clear();
        else this.workCities = workCities;
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
