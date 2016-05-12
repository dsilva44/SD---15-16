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

public class TamperingHandler implements SOAPHandler<SOAPMessageContext> {

    static final Logger log = LogManager.getRootLogger();

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext smc) {
        Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        try {
            if (outboundElement){
                handleOutboundMessage(smc);
            }
        } catch (Exception e) {
            log.error(e);
        }
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {

    }

    public void handleOutboundMessage(SOAPMessageContext smc) throws Exception{

        SOAPMessage message = smc.getMessage();
        SOAPPart sp = message.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPBody sb = se.getBody();

        Name name = se.createName("requestJob", "ns2", "http://ws.transporter.upa.pt/");
        Iterator bodyIT = sb.getChildElements(name);
        if(bodyIT.hasNext()==false){
            return;
        }
        SOAPElement reqJobElement = (SOAPElement) bodyIT.next();

        name = se.createName("price", "", "");
        Iterator reqJobIT = reqJobElement.getChildElements(name);
        if(reqJobIT.hasNext()==false){
            return;
        }
        SOAPElement priceElement = (SOAPElement) reqJobIT.next();

        if(priceElement.getValue().equals("5")){
            priceElement.removeContents();
            priceElement.addTextNode("1");
        }

    }

}
