package pt.upa.ca.ws;

import javax.jws.WebService;

@WebService(endpointInterface = "pt.upa.ca.ws.CA")
public class CAImpl implements CA {
    @Override
    public byte[] getCertificate(String name) {
        String hello = "Hello World!";

        return hello.getBytes();
    }
}
