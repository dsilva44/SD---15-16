package pt.upa.ca.ws;

import javax.jws.WebService;
import javax.security.cert.Certificate;

@WebService
public interface CA {
    Certificate getCertificate(String name);
}
