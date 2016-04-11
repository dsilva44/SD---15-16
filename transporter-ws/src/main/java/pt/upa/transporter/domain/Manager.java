package pt.upa.transporter.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.transporter.exception.JobDoesNotExistException;
import pt.upa.transporter.exception.WrongStateToConfirmException;
import pt.upa.transporter.ws.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class Manager {
    static private final Logger log = LogManager.getRootLogger();
    private static Manager manager = new Manager();

    private String transporterName;
    private String transporterParity;
    private ArrayList<Job> jobs;
    private int jobID = 0;

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

        this.transporterName = transporterName;

        int tNum = Integer.parseInt(transporterName.substring(transporterName.length() - 1));
        if (tNum % 2 == 0) { transporterParity = "EVEN"; }
        else transporterParity = "ODD";

        workCities.addAll(centro);

        switch (transporterParity) {
            case "EVEN":
                workCities.addAll(norte);
                break;
            case "ODD":
                workCities.addAll(sul);
                break;
        }
    }

    String getNextJobID() {
        return transporterName + "_" + jobID++;
    }

    String getTransporterParity() { return transporterParity; }

    ArrayList<String> getWorkCities() { return workCities; }

    ArrayList<Job> getJobs() {
        return jobs;
    }

    public Job decideResponse(String origin, String destination, int price) {
        int offerPrice;
        Job offerJob = new Job(transporterName, getNextJobID(), origin, destination, 0, JobStateView.PROPOSED);

        if (!workCities.contains(origin) || !workCities.contains(destination) || price == 0 || price > 100)
            return null;
        else if (price <= 10)
            offerPrice = ThreadLocalRandom.current().nextInt(1, 9);
        else if ((price % 2 == 0 & transporterParity.equals("EVEN")) ||
                (price % 2 != 0 & transporterParity.equals("ODD")))
            offerPrice = ThreadLocalRandom.current().nextInt(1, price-1);
        else
            offerPrice = ThreadLocalRandom.current().nextInt(price+1, 1000);

        offerJob.setJobPrice(offerPrice);
        jobs.add(offerJob);
        return offerJob;
    }

    public void validateRequestedJob(String origin, String destination, int price)
            throws BadLocationFault_Exception, BadPriceFault_Exception {
        class BadFaultLocation {
            private void throwException(String location) throws BadLocationFault_Exception {
                BadLocationFault faultInfo = new BadLocationFault();
                faultInfo.setLocation(location);

                log.warn(location + " is a unknown location");

                throw new BadLocationFault_Exception(
                        location + " is a unknown location",
                        faultInfo);
            }
        }

        if (!knowCities.contains(origin)) new BadFaultLocation().throwException(origin);
        if (!knowCities.contains(destination)) new BadFaultLocation().throwException(destination);
        if (price < 0) {
            BadPriceFault faultInfo = new BadPriceFault();
            faultInfo.setPrice(price);
            log.warn(price + " is not valid");
            throw new BadPriceFault_Exception(price + " is not valid", faultInfo);
        }
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
