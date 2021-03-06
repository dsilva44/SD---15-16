package pt.upa.transporter.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.transporter.ws.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import java.util.Timer;
import java.util.TimerTask;

public class Manager  {
    static private final Logger log = LogManager.getRootLogger();
    private static Manager manager = new Manager();

    private String transporterName;
    private String keyStorePath;
    private String password;
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

    public void init(String transporterName, String keyStorePath, String password) {
        this.keyStorePath = keyStorePath;
        this.password = password;
        init(transporterName);
    }

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

    public ArrayList<Job> getJobs() {
        return jobs;
    }

    public String getKeyStorePath(){ return keyStorePath;}

    public String getTransporterName(){ return transporterName;}
    public String getPassword(){ return password; }

    public Job decideResponse(String origin, String destination, int price)
            throws BadLocationFault_Exception, BadPriceFault_Exception {

        validateRequestedJob(origin, destination, price);

        int offerPrice;
        Job offerJob = new Job(transporterName, getNextJobID(), origin, destination, 0, JobStateView.PROPOSED);

        if (!containsCaseInsensitive(origin, workCities) || !containsCaseInsensitive(destination, workCities) ||
                price > 100)
            return null;
        else if (price == 0 || price == 1)
            offerPrice = 0;
        else if (price <= 10) {
            offerPrice = genRandomTime(1, price);
        }
        else if ((price % 2 == 0 & transporterParity.equals("EVEN")) ||
                (price % 2 != 0 & transporterParity.equals("ODD")))
            offerPrice = genRandomTime(1, price);
        else
            offerPrice = genRandomTime(price+1, 1000);

        offerJob.setJobPrice(offerPrice);
        jobs.add(offerJob);

        log.debug("reference price: "+price+" offerPrice: "  + offerPrice+" offerID: "+offerJob.getJobIdentifier());
        return offerJob;
    }

    void validateRequestedJob(String origin, String destination, int price)
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

        if (!containsCaseInsensitive(origin, knowCities)) new BadFaultLocation().throwException(origin);
        if (!containsCaseInsensitive(destination, knowCities)) new BadFaultLocation().throwException(destination);
        if (price < 0) {
            BadPriceFault faultInfo = new BadPriceFault();
            faultInfo.setPrice(price);
            log.warn(price + " is not valid");
            throw new BadPriceFault_Exception(price + " is not a valid price", faultInfo);
        }
    }

    private int genRandomTime(int min, int max){
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    public void transportSimulation(Job j) {

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
    		@Override
        	public void run() {
                if (!j.isCompleted()) j.nextState();
                else this.cancel();
    		}
    	}, genRandomTime(1000, 5000), genRandomTime(1000, 5000));
    }

    public Job confirmationJobs(String id, boolean bool) throws BadJobFault_Exception {

        Job job = getJobById(id);
        if(job == null){
            BadJobFault fault = new BadJobFault();
            fault.setId(id);
            throw new BadJobFault_Exception("not existing id", fault);
        }
        
        else if (job.getJobState() != JobStateView.PROPOSED) {
            BadJobFault fault = new BadJobFault();
            fault.setId(id);
            throw new BadJobFault_Exception("invalid job state to confirm", fault);
        }
        else if (bool) {
            job.setJobState(JobStateView.ACCEPTED);
            transportSimulation(job);
        }
        else {
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

    private boolean containsCaseInsensitive(String s, List<String> l) {
        for (String string : l){
            if (string.equalsIgnoreCase(s)){
                return true;
            }
        }
        return false;
    }
}
