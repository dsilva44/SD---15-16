package pt.upa.ca.ws;

import javax.jws.WebService;
import javax.security.cert.Certificate;

@WebService(endpointInterface = "pt.upa.ca.ws.CA")
public class CAImpl implements CA {
    @Override
    public Certificate getCertificate(String name) {
        //TODO
        return null;
    }
}
