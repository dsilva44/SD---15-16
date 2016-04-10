package pt.upa.transporter.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pt.upa.transporter.ws.JobView;

public class ManagerTest {
	
    // static members
	static Manager m = Manager.getInstance();

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
	public void successSetJobsShouldClearList() {
		JobView job1 = new JobView();
		m.init("UpaTransporter1");
		m.addJob(job1);
		
		m.setJobs(null);
        assertEquals(0,m.getJobs().size());
    }
	
	@Test
	public void successGetJobViewExisting() {
		JobView job1 = new JobView();
		job1.setJobIdentifier("id1");
		m.init("UpaTransporter1");
		m.addJob(job1);
		
		assertEquals(job1,m.getJobView("id1"));
	}
	
	@Test
	public void successGetJobViewNonExisting(){
		m.init("UpaTransporter1");
		assertNull(m.getJobView("id"));
	}
	
}
