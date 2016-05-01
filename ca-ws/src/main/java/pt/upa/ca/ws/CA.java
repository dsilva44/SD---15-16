package pt.upa.ca.ws;

import pt.upa.ca.domain.Manager;
import pt.upa.ca.exception.CAException;

import java.security.KeyStore;
import javax.jws.WebService;
import java.security.cert.Certificate;

@WebService(endpointInterface = "pt.upa.ca.ws.CAPortType")
public class CA implements CAPortType {
    @Override
    public byte[] getCertificateFile(String subjectName) throws CAException {
        try {
            KeyStore keystore = Manager.getInstance().readKeyStoreFile();
            Certificate certificate = keystore.getCertificate(subjectName);
            return certificate.getEncoded();
        } catch (Exception e) {
            throw new CAException(e);
        }
    }
}
