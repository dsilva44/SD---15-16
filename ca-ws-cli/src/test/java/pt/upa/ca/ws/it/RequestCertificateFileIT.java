package pt.upa.ca.ws.it;

import org.junit.Test;
import pt.upa.ca.ws.CAException_Exception;

import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.cert.Certificate;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class RequestCertificateFileIT extends AbstractIT {
    private final String upaCAClientAlias = "UpaCAClient";
    private static final String keyStorePath = "src/test/resources/UpaCAClient.jks";
    private static final String keyStorePass = "passUpaCAClient";

    @Test
    public void successGetCertificateFile() throws Exception {
        byte[] certBytes = CLIENT.requestCertificateFile(upaCAClientAlias);
        Certificate caCliCert = CLIENT.toCertificate(certBytes);

        assertNotNull(caCliCert);
    }

    @Test(expected = CAException_Exception.class)
    public void aliasDosNotExist() throws Exception {
        CLIENT.requestCertificateFile("InvalidAlias");
    }

    @Test
    public void receiveCertificateSignByUpaCA() throws Exception {
        KeyStore keyStore = CLIENT.readKeyStoreFile(keyStorePath, keyStorePass.toCharArray());
        Certificate caCert = keyStore.getCertificate("UpaCA");

        // Request UPACAClient Certificate from UpaCA
        byte[] certBytes = CLIENT.requestCertificateFile(upaCAClientAlias);
        Certificate caCliCert = CLIENT.toCertificate(certBytes);

        try {
            caCliCert.verify(caCert.getPublicKey());
        } catch (InvalidKeyException e) {
            fail();
        }
    }
}
