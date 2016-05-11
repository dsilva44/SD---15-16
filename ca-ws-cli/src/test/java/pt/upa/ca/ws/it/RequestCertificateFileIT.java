package pt.upa.ca.ws.it;

import org.junit.Test;
import pt.upa.ca.exception.CAClientException;
import pt.upa.ca.ws.CAException_Exception;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class RequestCertificateFileIT extends AbstractIT {
    private final String upaCAClientAlias = "UpaCAClient";
    private static final String keyStorePath = "src/test/resources/UpaCAClient.jks";
    private static final String keyStorePass = "passUpaCAClient";

    @Test
    public void successGetCertificateFile() throws Exception {
        byte[] certBytes = CLIENT.requestCertificateFile(upaCAClientAlias);
        Certificate caCliCert = toCertificate(certBytes);

        assertNotNull(caCliCert);
    }

    @Test(expected = CAException_Exception.class)
    public void aliasDosNotExist() throws Exception {
        CLIENT.requestCertificateFile("InvalidAlias");
    }

    @Test
    public void receiveCertificateSignByUpaCA() throws Exception {
        KeyStore keyStore = readKeyStoreFile(keyStorePath, keyStorePass.toCharArray());
        Certificate caCert = keyStore.getCertificate("UpaCA");

        // Request UPACAClient Certificate from UpaCA
        byte[] certBytes = CLIENT.requestCertificateFile(upaCAClientAlias);
        Certificate caCliCert = toCertificate(certBytes);

        try {
            caCliCert.verify(caCert.getPublicKey());
        } catch (InvalidKeyException e) {
            fail();
        }
    }

    private Certificate toCertificate(byte[] certBytes) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return cf.generateCertificate(new ByteArrayInputStream(certBytes));
    }

    private KeyStore readKeyStoreFile(String keyStoreFilePath, char[] keyStorePassword) throws Exception {
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
}
