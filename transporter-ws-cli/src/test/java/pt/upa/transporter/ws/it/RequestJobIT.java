package pt.upa.transporter.ws.it;

import com.sun.xml.ws.fault.ServerSOAPFaultException;
import org.junit.Test;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class RequestJobIT extends AbstractIT {

    /*----------------------------------------------T_27-Tests--------------------------------------------------------*/
    @Test(expected = BadLocationFault_Exception.class)
    public void unknownOriginShouldThewException() throws Exception {
        int referencePrice = 50;

        CLIENT1.requestJob("BATATA", CENTRO_1, referencePrice);
    }

    @Test(expected = BadLocationFault_Exception.class)
    public void unknownDestinationShouldThewException() throws Exception  {
        int referencePrice = 50;

        CLIENT1.requestJob(CENTRO_2, "BATATA", referencePrice);
    }

    @Test(expected = BadPriceFault_Exception.class)
    public void negativePriceShouldThewException() throws Exception  {
        int referencePrice = -100;

        CLIENT1.requestJob(CENTRO_1, CENTRO_2, referencePrice);
    }

    @Test
    public void shouldReturnNullOnInvalidWorkZoneOrigin() throws Exception  {
        int referencePrice = 50;
        String evenLocation = "Braga";
        JobView returnRequestJob = CLIENT1.requestJob(evenLocation, CENTRO_1, referencePrice);

        assertNull("not return null", returnRequestJob);
    }

    @Test
    public void shouldReturnNullOnInvalidWorkZoneDestination() throws Exception  {
        int referencePrice = 50;
        String oddLocation = "Faro";

        JobView returnRequestJob = CLIENT2.requestJob(CENTRO_1, oddLocation, referencePrice);

        assertNull("not return null", returnRequestJob);
    }

    @Test
    public void shouldReturnNullOnPriceGreaterThan100() throws Exception  {
        int referencePrice = 101;

        JobView returnRequestJob = CLIENT1.requestJob(CENTRO_1, CENTRO_2, referencePrice);

        assertNull("not return null", returnRequestJob);
    }

    @Test
    public void priceEqualTo10shouldReturnPriceLessThen10AndGreaterEqualTo0()
            throws BadLocationFault_Exception , BadPriceFault_Exception {
        int referencePrice = 10;

        JobView returnRequestJob = CLIENT1.requestJob(CENTRO_1, CENTRO_2, referencePrice);

        assertTrue("price is not less then 10", (returnRequestJob.getJobPrice() >= 0) &
                (returnRequestJob.getJobPrice() < 10));

    }

    @Test
    public void priceLessThen10shouldReturnPriceLessThen10AndGreaterEqualTo0()
            throws BadLocationFault_Exception, BadPriceFault_Exception  {
        int referencePrice = 3;

        JobView returnRequestJob = CLIENT1.requestJob(CENTRO_1, CENTRO_2, referencePrice);

        assertTrue("price is not less then 10", returnRequestJob.getJobPrice() >= 0 &
                (returnRequestJob.getJobPrice() < 10));
    }

    @Test
    public void priceEqualTo0shouldReturn0()
            throws BadLocationFault_Exception, BadPriceFault_Exception  {

        JobView returnRequestJob = CLIENT1.requestJob(CENTRO_1, CENTRO_2, 0);

        assertTrue("price is not less then 10", returnRequestJob.getJobPrice() == 0);
    }

    @Test
    public void oddPriceGreaterThan10LessOrEqualTo100AndOddTransporterShouldReturnPriceBelowReference()
            throws BadLocationFault_Exception, BadPriceFault_Exception {
        int referencePrice = 99;

        JobView returnRequestJob = CLIENT1.requestJob(CENTRO_1, CENTRO_2, referencePrice);

        assertTrue("price is not less then reference price", returnRequestJob.getJobPrice() >= 0 &
                (returnRequestJob.getJobPrice() < referencePrice));
    }

    @Test
    public void evenPriceGreaterThan10LessOrEqualTo100AndEvenTransporterShouldReturnPriceBelowReference()
            throws BadLocationFault_Exception, BadPriceFault_Exception {
        int referencePrice = 100;

        JobView returnRequestJob = CLIENT2.requestJob(CENTRO_1, CENTRO_2, referencePrice);

        assertNotNull("job is null", returnRequestJob);
        assertTrue("price is not less then reference price", returnRequestJob.getJobPrice() >= 0 &
                (returnRequestJob.getJobPrice() < referencePrice));
    }

    @Test
    public void oddPriceGreaterThan10LessOrEqualTo100AndEvenTransporterShouldReturnPriceAboveReference()
            throws BadLocationFault_Exception, BadPriceFault_Exception {
        int referencePrice = 11;

        JobView returnDecideResponse = CLIENT2.requestJob(CENTRO_1, CENTRO_2, referencePrice);

        assertTrue("price is not above reference price", returnDecideResponse.getJobPrice() >= 0 &
                (returnDecideResponse.getJobPrice() > referencePrice));
    }

    @Test
    public void evenPriceGreaterThan10LessOrEqualTo100AndOddTransporterShouldReturnPriceAboveReference()
            throws BadLocationFault_Exception, BadPriceFault_Exception {
        int referencePrice = 16;

        JobView returnDecideResponse = CLIENT1.requestJob(CENTRO_1, CENTRO_2, referencePrice);

        assertTrue("price is not greater reference price", returnDecideResponse.getJobPrice() >= 0 &
                (returnDecideResponse.getJobPrice() > referencePrice));
    }

    /*----------------------------------------------SD-Tests----------------------------------------------------------*/

    /**
     * Request a job (with valid origin, destination and price) with a price of
     * 10.
     *
     * @result The job should be successfully created and stored by the
     *         transporter.
     * @throws Exception
     */
    @Test
    public void testRequestJob() throws Exception {
        CLIENT1.requestJob(CENTRO_1, SUL_1, PRICE_SMALLEST_LIMIT);
    }

    // -------------- invalid inputs test cases ---------------

    /**
     * Invoke CLIENT.requestJob on an invalid (empty string) origin.
     *
     * @result Should throw BadLocationFault_Exception as the origin is invalid.
     * @throws Exception
     */
    @Test(expected = BadLocationFault_Exception.class)
    public void testRequestJobInvalidOrigin() throws Exception {
        CLIENT1.requestJob(EMPTY_STRING, CENTRO_1, PRICE_SMALLEST_LIMIT);
    }

    /**
     * Invoke CLIENT.requestJob on an invalid (null) origin.
     *
     * @result Should throw BadLocationFault_Exception as the origin is invalid.
     * @throws Exception
     */
    @Test(expected = BadLocationFault_Exception.class)
    public void testRequestJobNullOrigin() throws Exception {
        CLIENT1.requestJob(null, SUL_1, PRICE_SMALLEST_LIMIT);
    }

    /**
     * Invoke CLIENT.requestJob on an invalid (empty string) destination.
     *
     * @result Should throw BadLocationFault_Exception as the destination is
     *         invalid.
     * @throws Exception
     */
    @Test(expected = BadLocationFault_Exception.class)
    public void testRequestJobInvalidDestination() throws Exception {
        CLIENT1.requestJob(CENTRO_1, EMPTY_STRING, PRICE_SMALLEST_LIMIT);
    }

    /**
     * Invoke CLIENT.requestJob on an invalid (null) destination.
     *
     * @result Should throw BadLocationFault_Exception as the destination is
     *         invalid.
     * @throws Exception
     */
    @Test(expected = BadLocationFault_Exception.class)
    public void testRequestJobNullDestination() throws Exception {
        CLIENT1.requestJob(SUL_1, null, PRICE_SMALLEST_LIMIT);
    }

    /**
     * Invoke CLIENT.requestJob on both invalid (empty string) origin and
     * destination.
     *
     * @result Should throw BadLocationFault_Exception as both the origin and
     *         the destination is invalid.
     * @throws Exception
     */
    @Test(expected = BadLocationFault_Exception.class)
    public void testRequestJobInvalidOD() throws Exception {
        CLIENT1.requestJob(EMPTY_STRING, EMPTY_STRING, PRICE_SMALLEST_LIMIT);
    }

    /**
     * Invoke CLIENT.requestJob on both invalid (null) origin and destination.
     *
     * @result Should throw BadLocationFault_Exception as both the origin and
     *         the destination is invalid.
     * @throws Exception
     */
    @Test(expected = BadLocationFault_Exception.class)
    public void testRequestJobNullOD() throws Exception {
        CLIENT1.requestJob(null, null, PRICE_SMALLEST_LIMIT);
    }

    /**
     * Invoke CLIENT.requestJob with an invalid (negative) price.
     *
     * @result Should throw BadPriceFault_Exception as the price given was
     *         negative.
     * @throws Exception
     */
    @Test(expected = BadPriceFault_Exception.class)
    public void testRequestJobInvalidPrice() throws Exception {
        CLIENT1.requestJob(CENTRO_1, SUL_1, INVALID_PRICE);
    }

    /**
     * Invoke CLIENT.requestJob with all invalid parameters (empty string
     * locations and negative price) of origin, destination and price.
     *
     * @result Should throw BadLocationFault_Exception as both the origin and
     *         the destination are invalid or BadPriceFault_Exception as an
     *         invalid price given.
     * @throws Exception
     */
    public void testRequestJobInvalidArgs1() throws Exception {
        try {
            CLIENT1.requestJob(EMPTY_STRING, EMPTY_STRING, INVALID_PRICE);
        } catch (BadLocationFault_Exception | BadPriceFault_Exception e) {
            // do nothing because both exceptions can be expected
        }
    }

    /**
     * Invoke CLIENT.requestJob with all invalid parameters (null locations and
     * negative price) of origin, destination and price.
     *
     * @result Should throw BadLocationFault_Exception as both the origin and
     *         the destination are invalid or BadPriceFault_Exception as an
     *         invalid price given.
     * @throws Exception
     */
    public void testRequestJobInvalidArgs2() throws Exception {
        try {
            CLIENT1.requestJob(null, null, INVALID_PRICE);
        } catch (BadLocationFault_Exception | BadPriceFault_Exception e) {
            // do nothing because both exceptions can be expected
        }
    }

    // -------------- reference price > 100 ---------------

    /**
     * Test that a job request with a price over 100 returns null.
     *
     * @return A null JobView reference.
     * @throws Exception
     */
    @Test
    public void testUpperPriceLimit() throws Exception {
        JobView jv1 = CLIENT1.requestJob(SUL_1, CENTRO_1, PRICE_UPPER_LIMIT + 1);
        assertNull(jv1);
    }

    // -------------- reference price <= 10 ---------------

    /**
     * Test that a job requested with a price below 10 returns a positive price
     * lower or equal to 10.
     *
     * @throws Exception
     */
    @Test
    public void testPriceBelowSmallestLimit() throws Exception {
        final int referencePrice = PRICE_SMALLEST_LIMIT - UNITARY_PRICE;
        JobView jv1 = CLIENT1.requestJob(CENTRO_1, SUL_1, referencePrice);
        final int price = jv1.getJobPrice();
        assertTrue(price >= ZERO_PRICE && price < referencePrice);
    }

    /**
     * Test a job request with a price of 10. The proposed price should be
     * greater or equal to 0 and lower than 10.
     *
     * @result JobView with a price value under the constraint mentioned above.
     * @throws Exception
     */
    @Test
    public void testLowerEqualPriceLimit() throws Exception {
        final int referencePrice = PRICE_SMALLEST_LIMIT;
        JobView jv1 = CLIENT1.requestJob(SUL_1, CENTRO_1, referencePrice);
        final int price = jv1.getJobPrice();
        assertTrue(price >= ZERO_PRICE && price < referencePrice);
    }

    // -------------- reference price > 10 ---------------

    /**
     * Odd transporter, odd price
     *
     * Test that an odd-numbered transporter (e.g. UpaTransporter1) with an odd
     * price request returns a proposal between [1, price + 1[.
     *
     * @throws Exception
     */
    @Test
    public void testOddPriceAboveSmallestLimit() throws Exception {
        int oddReferencePrice = PRICE_SMALLEST_LIMIT + 1;
        assertTrue(oddReferencePrice % 2 == 1);

        JobView jv1 = CLIENT1.requestJob(CENTRO_1, SUL_1, oddReferencePrice);
        final int price = jv1.getJobPrice();
        assertTrue(price >= ZERO_PRICE && price < oddReferencePrice);
    }

    /**
     * Odd transporter, even price
     *
     * Test that an odd-numbered transporter (e.g. UpaTransporter1) with an even
     * price request of 12 returns a proposal between ]price,
     * Integer.MAX_VALUE[.
     *
     * @throws Exception
     */
    @Test
    public void testEvenPriceAboveSmallestLimit() throws Exception {
        int evenReferencePrice = PRICE_SMALLEST_LIMIT + 2;
        assertTrue(evenReferencePrice % 2 == 0);
        assertTrue(evenReferencePrice < Integer.MAX_VALUE - 1);

        JobView jv1 = CLIENT1.requestJob(CENTRO_1, SUL_1, evenReferencePrice);
        final int price = jv1.getJobPrice();
        assertTrue(price > evenReferencePrice && price < Integer.MAX_VALUE);
    }

    // -------------- reference price border cases ---------------

    /**
     * Test that a job request with a price of 1 returns a proposal with a price
     * of 0.
     *
     * @return JobView reference with price set to 0.
     * @throws Exception
     */
    @Test
    public void testZeroPrice() throws Exception {
        JobView jv1 = CLIENT1.requestJob(SUL_1, CENTRO_1, ZERO_PRICE);
        final int price = jv1.getJobPrice();
        assertEquals(ZERO_PRICE, price);
    }

    /**
     * Test that a job request with a price of 1 returns a proposal with a price
     * of 0.
     *
     * @return JobView reference with price set to 0.
     * @throws Exception
     */
    @Test
    public void testUnitaryPrice() throws Exception {
        JobView jv1 = CLIENT1.requestJob(SUL_1, CENTRO_1, UNITARY_PRICE);
        final int price = jv1.getJobPrice();
        assertEquals(ZERO_PRICE, price);
    }

    // ------------------------- DEMONSTRATION TESTS -------------------------

    @Test(expected = ServerSOAPFaultException.class)
    public void testEnableTamperingHandler() throws Exception {
        CLIENT1.requestJob(CENTRO_1, CENTRO_2, 5);
    }

}
