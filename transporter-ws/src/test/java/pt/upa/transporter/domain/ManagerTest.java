package pt.upa.transporter.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pt.upa.transporter.ws.JobView;

public class ManagerTest {
	
    // static members
	static Manager m = new Manager("upa1");

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() {
		JobView job1 = new JobView();
		JobView job2 = new JobView();
		JobView job3 = new JobView();
		
		List<JobView> list = null;
		list.add(job1);
		list.add(job2);
		list.add(job3);
    	m.setJobs(list);
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
	public void successJobListShouldBeEmpty() {
		m.setJobs(null);
        assertEquals("Job list should be empty", 0,m.getJobs().size());
    }
}
