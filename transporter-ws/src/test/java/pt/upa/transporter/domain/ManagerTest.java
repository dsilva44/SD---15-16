package pt.upa.transporter.domain;

import pt.upa.transporter.exception.WrongStateToConfirmException;
import pt.upa.transporter.exception.JobDoesNotExistException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobStateView;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ManagerTest {
	
    // static members

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() {
    }

    @AfterClass
    public static void oneTimeTearDown() {

    }


    // members
    private Manager manager = Manager.getInstance();

    private Job validJob = new Job("UpaTransporter1", "validjobtest", "Lisboa", "Leiria", 50, JobStateView.PROPOSED);
    private Job invalidJob = new Job( "UpaTransporter1", "invalidjobtest", "Lisboa", "Leiria", 50, JobStateView.HEADING);

    private final ArrayList<String> centro = new ArrayList<>(Arrays.asList("Lisboa", "Leiria", "Santarém",
            "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda"));
    private final ArrayList<String> norte = new ArrayList<>(Arrays.asList("Porto", "Braga", "Viana do Castelo",
            "Vila Real", "Bragança"));
    private final ArrayList<String> sul = new ArrayList<>(Arrays.asList("Setúbal", "Évora", "Portalegre", "Beja",
            "Faro"));

    private String centroLocation1 = "Lisboa";
    private String centroLocation2 = "Leiria";
    private String unknownLocation = "BATATA";
    private String oddLocation = "Faro";
    private String evenLocation = "Braga";

    // initialization and clean-up for each test
    @Before
    public void setUp() {
        manager.addJob(validJob);
        manager.addJob(invalidJob);
    }

    @After
    public void tearDown() {
        manager.removeJob(validJob);
        manager.removeJob(invalidJob);
        manager.setWorkCities(null);
    }

    // -------------------------------------------------- Setters ------------------------------------------------------
	@Test
	public void successSetNullJobsShouldClearList() {
		Job job1 = new Job();
		manager.addJob(job1);

		manager.setJobs(null);
        assertEquals(0, manager.getJobs().size());
    }

    @Test
    public void successSetNullWorkCitiesShouldClearList() {
        manager.init("UpaTransporter1");

        assertFalse("work cities is empty", manager.getWorkCities().isEmpty());

        manager.setWorkCities(null);

        assertNotNull("work cities attribute is null", manager.getWorkCities());
        assertTrue("work cities is not empty", manager.getWorkCities().isEmpty());
    }

    @Test
    public void successSetNewWorkCities() {
        ArrayList<String> newArrayList = new ArrayList<>();

        manager.setWorkCities(newArrayList);

        assertEquals("wrong set of array", newArrayList, manager.getWorkCities());
    }
    // ---------------------------------------- init(String transporterName) -------------------------------------------

    @Test
    public void successODDTransporterInit() {
        String validTransporterName = "UpaTransporter983";
        ArrayList<String> workCitiesODD = new ArrayList<>();
        workCitiesODD.addAll(centro);
        workCitiesODD.addAll(sul);

        manager.init(validTransporterName);

        assertEquals("wrong parity", "ODD", manager.getParity());
        assertEquals("wrong work cities", workCitiesODD, manager.getWorkCities());
    }

    @Test
    public void successEVENTransporterInit() {
        String validTransporterName = "UpaTransporter4864";

        ArrayList<String> workCitiesEVEN = new ArrayList<>();
        workCitiesEVEN.addAll(centro);
        workCitiesEVEN.addAll(norte);

        manager.init(validTransporterName);

        assertEquals("wrong parity", "EVEN", manager.getParity());
        assertEquals("wrong work cities", workCitiesEVEN, manager.getWorkCities());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTransporterNameInitShouldReturnException() {
        String validTransporterName = "BATATA";

        manager.init(validTransporterName);
    }
    // -----------------------------------------------------------------------------------------------------------------

    @Test
	public void successGetJobViewExisting() {
		Job job1 = new Job();
		job1.setJobIdentifier("id1");
		manager.addJob(job1);

		assertEquals(job1, manager.getJobById("id1"));
	}

	@Test
	public void successGetJobViewNonExisting(){
		assertNull(manager.getJobById("id"));
	}


    // ----------------------------------------confirmationJob(String id)--------------------------------------------------

    @Test(expected=JobDoesNotExistException.class)
    public void ConfirmJobWithInvalidIDTest() throws Exception{

        manager.confirmationJobs("bananas", true);
    }

    @Test(expected=WrongStateToConfirmException.class)
    public void trueConfirmJobWithWrongStateTest() throws Exception {

       manager.confirmationJobs("invalidjobtest", true);
    }

    @Test
    public void trueConfirmJobWithCorrectStateTest() throws Exception{
        Job job = manager.confirmationJobs("validjobtest", true);

        assertEquals("confirmation job did not work correctly", job.getJobState(), JobStateView.ACCEPTED);
    }

    @Test(expected = WrongStateToConfirmException.class)
    public void falseConfirmWithWrongStateJobTest() throws Exception{
        manager.confirmationJobs("invalidjobtest", false);
    }

    @Test
    public void falseConfirmWithCorrectStateJobTest() throws Exception{
        Job job = manager.confirmationJobs("validjobtest", false);

        assertEquals("confirmation job did not work correctly", job.getJobState(), JobStateView.REJECTED) ;
    }

    // -------------------------decideResponse(String origin, String destination, int price)----------------------------

    @Test(expected = BadLocationFault_Exception.class)
    public void unknownOriginShouldThewException() {
        int referencePrice = 50;
        manager.init("UpaTransporter1");

        manager.decideResponse(unknownLocation, centroLocation1, referencePrice);
    }

    @Test(expected = BadLocationFault_Exception.class)
    public void unknownDestinationShouldThewException() {
        int referencePrice = 50;
        manager.init("UpaTransporter2");

        manager.decideResponse(centroLocation1, unknownLocation, referencePrice);
    }

    @Test(expected = BadPriceFault_Exception.class)
    public void negativePriceShouldThewException() {
        int referencePrice = -100;
        manager.init("UpaTransporter3");

        manager.decideResponse(centroLocation1, centroLocation2, referencePrice);
    }

    @Test
    public void shouldReturnNullOnOriginThatDontWork() {
        int referencePrice = 50;
        manager.init("UpaTransporter1");

        Job returnDecideResponse = manager.decideResponse(evenLocation, centroLocation1, referencePrice);

        assertNull("not return null", returnDecideResponse);
    }

    @Test
    public void shouldReturnNullOnInvalidDestination() {
        int referencePrice = 50;
        manager.init("UpaTransporter2");

        Job returnDecideResponse = manager.decideResponse(centroLocation1, oddLocation, referencePrice);

        assertNull("not return null", returnDecideResponse);
    }

    @Test
    public void shouldReturnNullOnPriceGreaterThan100() {
        int referencePrice = 101;
        manager.init("UpaTransporter1");

        Job returnDecideResponse = manager.decideResponse(centroLocation1, centroLocation2, referencePrice);

        assertNull("not return null", returnDecideResponse);
    }

    @Test
    public void priceReferenceEqualTo10shouldReturnPriceLessThen10AndGreaterThan0() {
        int referencePrice = 10;
        manager.init("UpaTransporter1");

        Job returnDecideResponse = manager.decideResponse(centroLocation1, centroLocation2, referencePrice);

        assertNotNull("job is null", returnDecideResponse);
        assertTrue("price is not less then 10", (returnDecideResponse.getJobPrice() > 0) &
                                                    (returnDecideResponse.getJobPrice() < 10));

    }

    @Test
    public void priceReferenceLessThen10shouldReturnPriceLessThen10AndGreaterThan0() {
        int referencePrice = 5;
        manager.init("UpaTransporter1");

        Job returnDecideResponse = manager.decideResponse(centroLocation1, centroLocation2, referencePrice);

        assertNotNull("job is null", returnDecideResponse);
        assertTrue("price is not less then 10", returnDecideResponse.getJobPrice() > 0 &
                                                    (returnDecideResponse.getJobPrice() < 10));
    }

    @Test
    public void priceReferenceEqualTo0shouldReturnNull() {
        manager.init("UpaTransporter1");

        Job returnDecideResponse = manager.decideResponse(centroLocation1, centroLocation2, 0);

        assertNull("job is not null", returnDecideResponse);
    }

    @Test
    public void oddPriceReferenceGreaterThan10LessOrEqualTo100AndOddTransporterShouldReturnPriceBelowReference() {
        int referencePrice = 99;
        manager.init("UpaTransporter1");

        Job returnDecideResponse = manager.decideResponse(centroLocation1, centroLocation2, referencePrice);

        assertNotNull("job is null", returnDecideResponse);
        assertTrue("price is not less then reference price", returnDecideResponse.getJobPrice() > 0 &
                (returnDecideResponse.getJobPrice() < referencePrice));
    }

    @Test
    public void evenPriceReferenceGreaterThan10LessOrEqualTo100AndEvenTransporterShouldReturnPriceBelowReference() {
        int referencePrice = 100;
        manager.init("UpaTransporter2");

        Job returnDecideResponse = manager.decideResponse(centroLocation1, centroLocation2, referencePrice);

        assertNotNull("job is null", returnDecideResponse);
        assertTrue("price is not less then reference price", returnDecideResponse.getJobPrice() > 0 &
                (returnDecideResponse.getJobPrice() < referencePrice));
    }

    @Test
    public void oddPriceReferenceGreaterThan10LessOrEqualTo100AndEvenTransporterShouldReturnPriceAboveReference() {
        int referencePrice = 11;
        manager.init("UpaTransporter1");

        Job returnDecideResponse = manager.decideResponse(centroLocation1, centroLocation2, referencePrice);

        assertNotNull("job is null", returnDecideResponse);
        assertTrue("price is not less then reference price", returnDecideResponse.getJobPrice() > 0 &
                (returnDecideResponse.getJobPrice() > referencePrice));
    }

    @Test
    public void evenPriceReferenceGreaterThan10LessOrEqualTo100AndOddTransporterShouldReturnPriceAboveReference() {
        int referencePrice = 15;
        manager.init("UpaTransporter2");

        Job returnDecideResponse = manager.decideResponse(centroLocation1, centroLocation2, referencePrice);

        assertNotNull("job is null", returnDecideResponse);
        assertTrue("price is not less then reference price", returnDecideResponse.getJobPrice() > 0 &
                (returnDecideResponse.getJobPrice() > referencePrice));
    }

}
