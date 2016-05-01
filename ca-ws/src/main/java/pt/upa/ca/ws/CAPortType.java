package pt.upa.ca.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface CAPortType {
    @WebMethod
    byte[] getCertificateFile(@WebParam(name = "subjectName") String subjectName);
}
