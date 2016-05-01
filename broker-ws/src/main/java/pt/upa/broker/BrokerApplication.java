package pt.upa.broker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.broker.ws.EndpointManager;
import pt.upa.ca.ws.cli.CAClient;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

public class BrokerApplication {
	static private final Logger log = LogManager.getRootLogger();

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 3) {
			log.error("Argument(s) missing!");
			log.error("Usage: java "+ BrokerApplication.class.getName() +" + uddiURL wsName wsURL");
			return;
		}

		String uddiURL = args[0];
		String wsName = args[1];
		String wsUrl = args[2];

		EndpointManager endpointManager = new EndpointManager(uddiURL, wsUrl);

		endpointManager.start();

		/*
		CAClient client = new CAClient(uddiURL);
		byte[] certBytes = client.getCertificateFile("UpaBroker");
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		Certificate brokerCert = cf.generateCertificate(new ByteArrayInputStream(certBytes));

		KeyStore keyStore = readKeyStoreFile("/home/fred/IdeaProjects/SD/upaTransporters/broker-ws/src/main/resources/UpaBroker.jks", "passUpaBroker");
		Certificate caCert = keyStore.getCertificate("UpaCA");

		brokerCert.verify(brokerCert.getPublicKey());
		*/

		if (endpointManager.awaitConnections()) {
			try {
				System.out.println("Press enter to shutdown");
				System.in.read();
			} catch (IOException e) {
				log.error("Error: ", e);
			}
		}
		endpointManager.stop();
	}

	/*
	public static KeyStore readKeyStoreFile(String keyStoreFilePath, String keyStorePassword) throws Exception {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(keyStoreFilePath);
		} catch (FileNotFoundException e) {
			log.warn("Keystore file <" + keyStoreFilePath + "> not fount.");
		}
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(fis, keyStorePassword.toCharArray());

		return keystore;
	}
	*/

}
