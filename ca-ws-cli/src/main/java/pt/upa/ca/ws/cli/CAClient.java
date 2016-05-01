package pt.upa.ca.ws.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.ca.exception.CAClientException;
import pt.upa.ca.ws.CAException_Exception;
import pt.upa.ca.ws.CAPortType;
import pt.upa.ca.ws.CAService;

import javax.xml.ws.BindingProvider;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Map;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

public class CAClient implements CAPortType {
    private static final Logger log = LogManager.getRootLogger();

    private CAPortType port;
    private String wsURL;
    private String uddiURL;
    private String wsName;

    public CAClient()  {}

    public CAClient(String uddiURL)  {
        this.uddiURL = uddiURL;
        this.wsName = "UpaCA";
        uddiLookup();
        createStub();
    }

    /** UDDI lookup */
    private void uddiLookup() {
        try {
            log.info("Contacting UDDI at " + uddiURL);
            UDDINaming uddiNaming = new UDDINaming(uddiURL);

            log.info("Looking for 'UpaBroker'");
            wsURL = uddiNaming.lookup(wsName);

        } catch (Exception e) {
            String msg = String.format("Client failed lookup on UDDI at %s!", uddiURL);
            throw new CAClientException(msg, e);
        }

        if (wsURL == null) {
            String msg = String.format(
                    "Service with name %s not found on UDDI at %s", wsName, uddiURL);
            throw new CAClientException(msg);
        }
    }

    /** Stub creation and configuration */
    private void createStub() {
        log.info("Creating stub ...");
        CAService service = new CAService();
        port = service.getCAPort();

        if (wsURL != null) {
            log.info("Setting endpoint address ....");
            BindingProvider bindingProvider = (BindingProvider) port;
            Map<String, Object> requestContext = bindingProvider.getRequestContext();
            requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
        }
    }

    /*-----------------------------------------------remote invocation methods----------------------------------------*/

    @Override
    public byte[] getCertificateFile(String subjectName) throws CAException_Exception {
        return port.getCertificateFile(subjectName);
    }

    /*-----------------------------------------------additional methods-----------------------------------------------*/
    /**
     * Reads a KeyStore from a file
     *
     * @return The read KeyStore
     * @throws Exception
     */
    public KeyStore readKeyStoreFile(String keyStoreFilePath, char[] keyStorePassword) throws Exception {
        FileInputStream fis;
        try {
            fis = new FileInputStream(keyStoreFilePath);
        } catch (FileNotFoundException e) {
            log.warn("Keystore file <" + keyStoreFilePath + "> not fount.");
            throw new CAClientException("Keystore file <" + keyStoreFilePath + "> not fount.");
        }
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(fis, keyStorePassword);

        return keystore;
    }

    /**
     * Reads a certificate from a file
     *
     * @return The read Certificate
     * @throws Exception
     */
    public Certificate readCertificateFile(String certificateFilePath) throws Exception {
        FileInputStream fis;

        try {
            fis = new FileInputStream(certificateFilePath);
        } catch (FileNotFoundException e) {
            log.warn("Certificate file <" + certificateFilePath + "> not fount.");
            throw new CAClientException("Certificate file <" + certificateFilePath + "> not fount.");
        }
        BufferedInputStream bis = new BufferedInputStream(fis);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        if (bis.available() > 0) {
            return cf.generateCertificate(bis);
        }

        bis.close();
        fis.close();
        log.warn("Nothing to read");
        throw new CAClientException("Nothing to read");
    }

    public Certificate toCertificate(byte[] certBytes) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return cf.generateCertificate(new ByteArrayInputStream(certBytes));
    }
}

