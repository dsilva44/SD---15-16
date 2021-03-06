package pt.upa.ca.ws.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.ca.exception.CAClientException;
import pt.upa.ca.ws.CAException_Exception;
import pt.upa.ca.ws.CAPortType;
import pt.upa.ca.ws.CAService;

import javax.xml.ws.BindingProvider;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Map;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

public class CAClient implements CAPortType {
    private static final Logger log = LogManager.getRootLogger();

    private CAPortType port;
    private String wsURL;
    private String uddiURL;
    private String wsName;

    public CAClient()  {}

    public CAClient(String uddiURL)  {
        this.uddiURL = uddiURL;
        this.wsName = "UpaCA";
        uddiLookup();
        createStub();
    }

    /** UDDI lookup */
    private void uddiLookup() {
        try {
            UDDINaming uddiNaming = new UDDINaming(uddiURL);
            wsURL = uddiNaming.lookup(wsName);

        } catch (Exception e) {
            String msg = String.format("Client failed lookup on UDDI at %s!", uddiURL);
            throw new CAClientException(msg, e);
        }

        if (wsURL == null) {
            String msg = String.format(
                    "Service with name %s not found on UDDI at %s", wsName, uddiURL);
            throw new CAClientException(msg);
        }
    }

    /** Stub creation and configuration */
    private void createStub() {
        CAService service = new CAService();
        port = service.getCAPort();

        if (wsURL != null) {
            BindingProvider bindingProvider = (BindingProvider) port;
            Map<String, Object> requestContext = bindingProvider.getRequestContext();
            requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
        }
    }

    /*-----------------------------------------------remote invocation methods----------------------------------------*/

    @Override
    public byte[] requestCertificateFile(String subjectName) throws CAException_Exception {
        return port.requestCertificateFile(subjectName);
    }

    @Override
    public String ping(String name) {
        return port.ping(name);
    }

}

