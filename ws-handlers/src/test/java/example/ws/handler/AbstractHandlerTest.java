package example.ws.handler;

import java.io.ByteArrayInputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;

import org.junit.AfterClass;
import org.junit.BeforeClass;


/**
 *  Abstract handler test suite
 */
public abstract class AbstractHandlerTest {

    // static members

    /** hello-ws SOAP request message captured with LoggingHandler */
    protected static final String HELLO_SOAP_REQUEST = "<S:Envelope " +
    "xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
    "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
    "<SOAP-ENV:Header/>" +
    "<S:Body>" +
    "<ns2:sayHello xmlns:ns2=\"http://ws.example/\">" +
    "<arg0>friend</arg0>" +
    "</ns2:sayHello>" +
    "</S:Body></S:Envelope>";

    /** hello-ws SOAP response message captured with LoggingHandler */
    protected static final String HELLO_SOAP_RESPONSE = "<S:Envelope " +
    "xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
    "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
    "<SOAP-ENV:Header/>" +
    "<S:Body>" +
    "<ns2:sayHelloResponse xmlns:ns2=\"http://ws.example/\">" +
    "<return>Hello friend!</return>" +
    "</ns2:sayHelloResponse>" +
    "</S:Body></S:Envelope>";

    protected static final String INBOUND_MESSAGE ="<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Header><fresh:Freshness xmlns:fresh=\"http://freshness\"><id:Identifier xmlns:id=\"http://identifier\">41</id:Identifier><time:Date xmlns:time=\"http://date\">2016-05-12T14:22:38</time:Date></fresh:Freshness><sname:SenderName xmlns:sname=\"http://senderName\">UpaTransporter1</sname:SenderName><mSig:MessageSignature xmlns:mSig=\"http://messageSignature\">JR6uDddLeNVZ00Nj8uutrlNo78pxiqnBQpXE62rivEjYXyasMKhNC2NL14PfyuMao63+eYhhHG9Ty7pjbCUo9sR74WpokszebnrzBMga4YickEqvpD2ZI1R8JXZAxSAJG5bZq4zw/dpitdUnlrd5M1xn0M3BYFMBJdeY2PLtvFIvdsnCPV5fXQ5yJjHNzBQiZ5vKVfTStcb6BqbeWL81IwhOKJF6WxCUxUhNIaFehI3LAyS0L3TBwQms1rPgFVuhzCt80KfMEhH3YyV1lGNoKyCp4TYDECS4xv/lM/10h39cpLSitraUhU9alI2mAtdDEPuWD4dhGWOzxDRxBwznYQ==</mSig:MessageSignature></SOAP-ENV:Header><S:Body><ns2:clearJobsResponse xmlns:ns2=\"http://ws.transporter.upa.pt/\"/></S:Body></S:Envelope>";


    /** SOAP message factory */
    protected static final MessageFactory MESSAGE_FACTORY;

    static {
        try {
            MESSAGE_FACTORY = MessageFactory.newInstance();
        } catch(SOAPException e) {
            throw new RuntimeException(e);
        }
    }


    // helper functions

    protected static SOAPMessage byteArrayToSOAPMessage(byte[] msg) throws Exception {
        ByteArrayInputStream byteInStream = new ByteArrayInputStream(msg);
        StreamSource source = new StreamSource(byteInStream);
        SOAPMessage newMsg = newMsg = MESSAGE_FACTORY.createMessage();
        SOAPPart soapPart = newMsg.getSOAPPart();
        soapPart.setContent(source);
        return newMsg;
    }


    // one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() {

    }

    @AfterClass
    public static void oneTimeTearDown() {

    }

}
