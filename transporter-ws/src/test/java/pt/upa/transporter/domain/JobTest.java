package pt.upa.transporter.domain;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;

import java.util.ArrayList;
import java.util.Arrays;

public class JobTest {

    // static members

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() {
    }

    @AfterClass
    public static void oneTimeTearDown() {

    }


    // members

    // initialization and clean-up for each test
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void successJobViewConversion() {
        Job job = new Job("UpaTransporter1", "123", "Lisboa", "Leiria", 500, JobStateView.PROPOSED);

        JobView jobView = new JobView();
        jobView.setCompanyName("UpaTransporter1");
        jobView.setJobIdentifier("123");
        jobView.setJobOrigin("Lisboa");
        jobView.setJobDestination("Leiria");
        jobView.setJobPrice(500);
        jobView.setJobState(JobStateView.PROPOSED);

        JobView jobViewConversion = job.toJobView();

        assertEquals("CompanyName is wrong", jobView.getCompanyName(), jobViewConversion.getCompanyName());
        assertEquals("JobIdentifier is wrong", jobView.getJobIdentifier(), jobViewConversion.getJobIdentifier());
        assertEquals("JobOrigin is wrong",jobView.getJobOrigin(), jobViewConversion.getJobOrigin());
        assertEquals("JobDestination is wrong", jobView.getJobDestination(), jobViewConversion.getJobDestination());
        assertEquals("JobPrice is wrong", jobView.getJobPrice(), jobViewConversion.getJobPrice());
        assertEquals("JobState is wrong", jobView.getJobState(), jobViewConversion.getJobState());
    }

}
