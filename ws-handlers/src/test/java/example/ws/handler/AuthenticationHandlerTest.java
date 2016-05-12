package example.ws.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Iterator;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.junit.Test;

import mockit.Mocked;
import mockit.StrictExpectations;

public class AuthenticationHandlerTest extends AbstractHandlerTest {

    // tests

    public static final String INVOKER_PROPERTY = "my.invoker.property";
    public static final String KSPATH_PROPERTY = "my.kspath.property";
    public static final String PASSWORD_PROPERTY = "my.password.property";

    @Test
    public void testAuthenticationHandlerOutbound(
            @Mocked final SOAPMessageContext soapMessageContext,
            @Mocked final KeyStore keyStore)
        throws Exception {

        AuthenticationHandler handler = new AuthenticationHandler();

        final String soapText = HELLO_SOAP_REQUEST;
        final SOAPMessage soapMessage = byteArrayToSOAPMessage(soapText.getBytes());
        final Boolean soapOutbound = true;

        // generate RSA KeyPair
        KeyPair keypair = generate();
        final PrivateKey key = keypair.getPrivate();

        new StrictExpectations(handler) {{

            soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            result = soapOutbound;

            soapMessageContext.get(INVOKER_PROPERTY);
            result = "UpaBroker";

            soapMessageContext.get(KSPATH_PROPERTY);
            result = "broker-ws/src/main/resources/UpaBroker.jks";

            soapMessageContext.get(PASSWORD_PROPERTY);
            result = "passUpaBroker";

            soapMessageContext.getMessage();
            result = soapMessage;

            handler.readKeyStoreFile("broker-ws/src/main/resources/UpaBroker.jks",
                    "passUpaBroker".toCharArray());
            result = keyStore;

            keyStore.getKey("UpaBroker", "passUpaBroker".toCharArray());
            result = key;
        }};

        boolean handleResult = handler.handleMessage(soapMessageContext);
        assertTrue(handleResult);

        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
        SOAPHeader soapHeader = soapEnvelope.getHeader();
        assertNotNull(soapHeader);

        Iterator headerIt = soapHeader.getChildElements();
        assertTrue(headerIt.hasNext());

        SOAPElement freshElement = (SOAPElement) headerIt.next();
        assertEquals("Freshness element is not correct", "Freshness", freshElement.getLocalName());

        assertTrue(headerIt.hasNext());
        SOAPElement senderElement = (SOAPElement) headerIt.next();
        assertEquals("SenderName element is not correct", "SenderName", senderElement.getLocalName());

        assertTrue(headerIt.hasNext());
        SOAPElement sigElement = (SOAPElement) headerIt.next();
        assertEquals("MessageSignature element is not correct", "MessageSignature", sigElement.getLocalName());

        Iterator freshIt = freshElement.getChildElements();
        assertTrue(freshIt.hasNext());

        SOAPElement idElement = (SOAPElement) freshIt.next();
        assertEquals("identifier element (from freshness) is not correct", "Identifier", idElement.getLocalName());

        assertTrue(freshIt.hasNext());
        SOAPElement dateElement = (SOAPElement) freshIt.next();
        assertEquals("date element (from freshness) is not correct", "Date", dateElement.getLocalName());
    }

    @Test
    public void testHeaderHandlerInbound(
        @Mocked final SOAPMessageContext soapMessageContext)
        throws Exception {

    }

    /** auxiliary method to generate KeyPair */
    public static KeyPair generate() throws Exception {
        // generate an RSA key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair key = keyGen.generateKeyPair();

        return key;
    }



}
