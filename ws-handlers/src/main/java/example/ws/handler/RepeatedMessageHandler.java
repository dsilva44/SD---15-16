package example.ws.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Iterator;
import java.util.Set;

public class RepeatedMessageHandler implements SOAPHandler<SOAPMessageContext> {
    private static final Logger log = LogManager.getRootLogger();

    public static final String OPR_ID_PROPERTY = "my.operationID.property";
    public static final String OPR_ID_HEADER = "myOprIDHeader";
    public static final String OPR_ID_NS = "urn:oprid";
    public static final String OPR_ID_PREFIX = "oprID";

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
                element.addTextNode(operationID);

            } catch (SOAPException e) {
                log.error("Failed to add SOAP header because of %s%n", e);
            }

        } else {
            try {
                SOAPEnvelope envelope = getSOAEnvelop(context);

                SOAPHeader header = envelope.getHeader();
                if (header == null) { log.error("Header not found."); return true; }

                SOAPElement element =  getHeaderElement(envelope, header);
                if (element == null) { log.error("Header element %s not found "+OPR_ID_HEADER); return true; }
                String headerValue = element.getValue();

                context.put(OPR_ID_PROPERTY, headerValue);
                context.setScope(OPR_ID_PROPERTY, MessageContext.Scope.APPLICATION);

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

    private SOAPElement getHeaderElement(SOAPEnvelope envelope, SOAPHeader header) throws SOAPException {
        Name name =  envelope.createName(OPR_ID_HEADER, OPR_ID_PREFIX, OPR_ID_NS);
        Iterator it = header.getChildElements(name);
        if (!it.hasNext())
            return null;
        return  (SOAPElement) it.next();
    }
}
