package pt.upa.ca.ws.cli;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.ca.exception.CAClientException;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import javax.xml.registry.JAXRException;

import java.security.KeyStore;

import static org.junit.Assert.*;

public class CAClientTest {

	// static members

	private static final String uddiURL = "http://localhost:9090";
	private static final String wsName = "UpaCA";

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

	// tests
	// assertEquals(expected, actual);

	@Test
	public void testMockUddi(@Mocked final UDDINaming uddiNaming) throws Exception {

		// Preparation code not specific to JMockit, if any.

		// an "expectation block"
		// One or more invocations to mocked types, causing expectations to be
		// recorded.
		new Expectations() {
			{
				new UDDINaming(uddiURL);
				uddiNaming.lookup(wsName); result = null; // hack
			}
		};

		// Unit under test is exercised.
		try {
			new CAClient(uddiURL);
		} catch (CAClientException e) {
			// Hack to pass the creation of stub
		}

		// a "verification block"
		// One or more invocations to mocked types, causing expectations to be
		// verified.
		//noinspection Duplicates
		new Verifications() {
			{
				// Verifies that zero or one invocations occurred, with the
				// specified argument value:
				new UDDINaming(uddiURL);
				uddiNaming.lookup(wsName);
				maxTimes = 1;
				uddiNaming.unbind(null);
				maxTimes = 0;
				uddiNaming.bind(null, null);
				maxTimes = 0;
				uddiNaming.rebind(null, null);
				maxTimes = 0;
			}
		};

		// Additional verification code, if any, either here or before the
		// verification block.
	}

	@Test
	public void testMockUddiNameNotFound(@Mocked final UDDINaming uddiNaming) throws Exception {

		// Preparation code not specific to JMockit, if any.

		// an "expectation block"
		// One or more invocations to mocked types, causing expectations to be
		// recorded.
		new Expectations() {
			{
				new UDDINaming(uddiURL);
				uddiNaming.lookup(wsName);
				result = null;
			}
		};

		// Unit under test is exercised.
		try {
			new CAClient(uddiURL);
			fail();

		} catch (CAClientException e) {
			final String expectedMessage = String.format("Service with name %s not found on UDDI at %s", wsName,
					uddiURL);
			assertEquals(expectedMessage, e.getMessage());
		}

		// a "verification block"
		// One or more invocations to mocked types, causing expectations to be
		// verified.
		new Verifications() {
			{
				// Verifies that zero or one invocations occurred, with the
				// specified argument value:
				new UDDINaming(uddiURL);
				uddiNaming.lookup(wsName);
				maxTimes = 1;
			}
		};

		// Additional verification code, if any, either here or before the
		// verification block.
	}

	@Test
	public void testMockUddiServerNotFound(@Mocked final UDDINaming uddiNaming) throws Exception {

		// Preparation code not specific to JMockit, if any.

		// an "expectation block"
		// One or more invocations to mocked types, causing expectations to be
		// recorded.
		new Expectations() {
			{
				new UDDINaming(uddiURL);
				result = new JAXRException("created for testing");
			}
		};

		// Unit under test is exercised.
		try {
			new CAClient(uddiURL);
			fail();

		} catch (CAClientException e) {
			assertTrue(e.getCause() instanceof JAXRException);
			final String expectedMessage = String.format("Client failed lookup on UDDI at %s!", uddiURL);
			assertEquals(expectedMessage, e.getMessage());
		}

		// a "verification block"
		// One or more invocations to mocked types, causing expectations to be
		// verified.
		new Verifications() {
			{
				// Verifies that zero or one invocations occurred, with the
				// specified argument value:
				new UDDINaming(uddiURL);
			}
		};

		// Additional verification code, if any, either here or before the
		// verification block.
	}

}
