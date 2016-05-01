package pt.upa.ca.ws;

import javax.jws.WebService;

@WebService(endpointInterface = "pt.upa.ca.ws.CAPortType")
public class CA implements CAPortType {
    @Override
    public byte[] getCertificateFile(String subjectName) {
        String hello = "Hello World!";

        return hello.getBytes();
    }
}
