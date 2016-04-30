package pt.upa.transporter.domain;

import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;

public class Job {
    private String companyName;
    private String jobIdentifier;
    private String jobOrigin;
    private String jobDestination;
    private int jobPrice;
    private JobStateView jobState;

    public Job () {

    }

    public Job(String companyName, String jobIdentifier, String jobOrigin, String jobDestination, int jobPrice, JobStateView jobState) {
        this.companyName = companyName;
        this.jobIdentifier = jobIdentifier;
        this.jobOrigin = jobOrigin;
        this.jobDestination = jobDestination;
        this.jobPrice = jobPrice;
        this.jobState = jobState;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobIdentifier() {
        return jobIdentifier;
    }

    public void setJobIdentifier(String jobIdentifier) {
        this.jobIdentifier = jobIdentifier;
    }

    public String getJobOrigin() {
        return jobOrigin;
    }

    public void setJobOrigin(String jobOrigin) {
        this.jobOrigin = jobOrigin;
    }

    public String getJobDestination() {
        return jobDestination;
    }

    public void setJobDestination(String jobDestination) {
        this.jobDestination = jobDestination;
    }

    public int getJobPrice() {
        return jobPrice;
    }

    public void setJobPrice(int jobPrice) {
        this.jobPrice = jobPrice;
    }

    public JobStateView getJobState() {
        return jobState;
    }

    public void setJobState(JobStateView jobState) {
        this.jobState = jobState;
    }

    public JobView toJobView() {
        JobView jobView = new JobView();

        jobView.setCompanyName(companyName);
        jobView.setJobDestination(jobDestination);
        jobView.setJobOrigin(jobOrigin);
        jobView.setJobIdentifier(jobIdentifier);
        jobView.setJobPrice(jobPrice);
        jobView.setJobState(jobState);

        return jobView;
    }

    public void nextState() {
        if (getJobState().equals(JobStateView.ACCEPTED)) setJobState(JobStateView.HEADING);
        else if (getJobState().equals(JobStateView.HEADING)) setJobState(JobStateView.ONGOING);
        else if (getJobState().equals(JobStateView.ONGOING)) setJobState(JobStateView.COMPLETED);
    }

    public boolean isCompleted() {
        return getJobState().equals(JobStateView.COMPLETED);
    }
}
