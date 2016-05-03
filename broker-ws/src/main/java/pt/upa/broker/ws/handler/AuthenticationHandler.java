package pt.upa.broker.ws.handler;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Set;

import javax.crypto.Cipher;
import javax.jws.HandlerChain;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;


public class AuthenticationHandler implements SOAPHandler<SOAPMessageContext> {

    //
    // Handler interface methods
    //
    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc){
        Boolean outboundElement = (Boolean) smc
                .get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        //sign outbound message
        if (outboundElement.booleanValue()) {
            handleOutboundMessage(smc);
        }

        //verify signature
        else{
            try {
                handleInboundMessage(smc);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean handleFault(SOAPMessageContext smc) {
        return true;
    }

    public void close(MessageContext messageContext) {
    }

    public void handleOutboundMessage(SOAPMessageContext smc){}

    public boolean handleInboundMessage(SOAPMessageContext smc) throws Exception{

        SOAPMessage msg = smc.getMessage();
        SOAPPart sp = msg.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPHeader sh = se.getHeader();

        // check header
        if (sh == null) {
            System.out.println("Header not found.");
            return true;
        }
        byte[] originalBytes = getSOAPBodyContent(msg);

            /*
            header = hash;
            body=m1;
            */


        byte[] hashedBytes = sh.getValue().getBytes();

            /*SOAPHeader sh = se.getHeader();//how to get url?
            String url = "";
            Key publicKey = CA.getCertificate(url).getPublicKey();*/

        return true;
        //return verifyDigitalSignature(hashedBytes,originalBytes,publicKey);
    }

    public byte[] getSOAPBodyContent(SOAPMessage sm) throws Exception{

        SOAPBody body = sm.getSOAPBody();

        //getting body string
        DOMSource source = new DOMSource(body);
        StringWriter stringResult = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
        String bodyString = stringResult.toString();
        byte[] soapBytes = bodyString.getBytes(StandardCharsets.UTF_8);
        return soapBytes;
    }


    public static boolean verifyDigitalSignature(byte[] cipherDigest,
                                                 byte[] text,
                                                 Key publicKey) throws Exception {

        // get a message digest object using the MD5 algorithm
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");

        // calculate the digest and print it out
        messageDigest.update(text);
        byte[] digest = messageDigest.digest();

        // get an RSA cipher object
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        // decrypt the ciphered digest using the public key
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] decipheredDigest = cipher.doFinal(cipherDigest);

        // compare digests
        if (digest.length != decipheredDigest.length)
            return false;

        for (int i=0; i < digest.length; i++)
            if (digest[i] != decipheredDigest[i])
                return false;
        return true;
    }
}