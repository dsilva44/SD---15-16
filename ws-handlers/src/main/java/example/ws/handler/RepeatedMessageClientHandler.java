package example.ws.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

public class RepeatedMessageClientHandler implements SOAPHandler<SOAPMessageContext> {
    private static final Logger log = LogManager.getRootLogger();

    public static final String OPR_ID_PROPERTY = "my.operationID.property";
    private static final String OPR_ID_HEADER = "myOprIDHeader";
    private static final String OPR_ID_NS = "urn:oprid";
    private static final String OPR_ID_PREFIX = "oprID";

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound) {
            String operationID = (String) context.get(OPR_ID_PROPERTY);
            try {
                SOAPEnvelope envelope = getSOAEnvelop(context);

                // add header
                SOAPHeader soapHeader = envelope.getHeader();
                if (soapHeader == null) soapHeader = envelope.addHeader();

                // add header element (name, namespace prefix, namespace)
                Name name = envelope.createName(OPR_ID_HEADER, OPR_ID_PREFIX, OPR_ID_NS);
                SOAPHeaderElement element = soapHeader.addHeaderElement(name);

                // add header element value
                if (operationID != null)
                    element.addTextNode(operationID);

            } catch (SOAPException e) {
                log.error("Failed to add SOAP header because of %s%n", e);
            }
        }

        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;
    }

    @Override
    public void close(MessageContext context) {

    }

    //-------------------------------------------Aux methods------------------------------------------------------------
    private SOAPEnvelope getSOAEnvelop(SOAPMessageContext context) throws SOAPException {
        SOAPMessage soapMessage = context.getMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        return soapPart.getEnvelope();
    }
}
