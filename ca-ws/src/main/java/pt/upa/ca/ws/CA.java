package pt.upa.ca.ws;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.ca.domain.Manager;
import pt.upa.ca.exception.CAException;

import java.security.KeyStore;
import javax.jws.WebService;
import java.security.cert.Certificate;

@WebService(endpointInterface = "pt.upa.ca.ws.CAPortType")
public class CA implements CAPortType {
    static private final Logger log = LogManager.getRootLogger();

    @Override
    public byte[] requestCertificateFile(String subjectName) throws CAException {
        log.debug("requestCertificateFile:");

        try {
            KeyStore keystore = Manager.getInstance().readKeyStoreFile();
            Certificate certificate = keystore.getCertificate(subjectName);
            return certificate.getEncoded();
        } catch (Exception e) {
            throw new CAException(e);
        }
    }

    @Override
    public String ping(String name) {
        log.debug("ping:");

        return "Pong " + name + "!";
    }
}
