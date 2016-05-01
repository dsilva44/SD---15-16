package pt.upa.ca.ws;

import pt.upa.ca.exception.CAException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface CAPortType {
    @WebMethod
    byte[] getCertificateFile(@WebParam(name = "subjectName") String subjectName) throws CAException;
}
