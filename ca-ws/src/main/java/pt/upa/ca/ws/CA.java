package pt.upa.ca.ws;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface CA {
    @WebMethod
    byte[] getCertificate(String name);
}
