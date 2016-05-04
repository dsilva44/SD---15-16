package pt.upa.broker.ws.handler;

import pt.upa.broker.domain.Manager;
import pt.upa.ca.ws.cli.CAClient;
import sun.misc.BASE64Encoder;

import java.io.StringWriter;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.crypto.Cipher;
import javax.jws.HandlerChain;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import static javax.xml.bind.DatatypeConverter.printHexBinary;


public class AuthenticationHandler implements SOAPHandler<SOAPMessageContext> {

    int idCounter = 0;

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
            try {
                handleOutboundMessage(smc);
            } catch (Exception e) {
                //System.out.printf("AUTHEN_OUTBOUND: Exception in handler: %s%n", e);
                //e.printStackTrace();
            }
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

    public void handleOutboundMessage(SOAPMessageContext smc) throws Exception{
        // get SOAP envelope
        SOAPMessage message = smc.getMessage();
        SOAPPart sp = message.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPBody sb = se.getBody();

        // add header
        SOAPHeader sh = se.getHeader();
        if (sh == null)
            sh = se.addHeader();


        String idString = "Broker" + idCounter++; //correct ?
        String dateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date());

        //add FRESHNESS header element and respective children
        Name name = se.createName("Freshness", "fresh", "http://freshness");
        SOAPElement freshnessElement = sh.addChildElement(name);

        // add idElement value
        name = se.createName("Identifier", "id", "http://identifier");
        SOAPElement idElement = freshnessElement.addChildElement(name).addTextNode(idString);

        // add timeElement value
        name = se.createName("Date", "time", "http://date");
        SOAPElement timeElement = freshnessElement.addChildElement(name).addTextNode(dateTime);

        String keyStorePath = Manager.getInstance().getKeyStorePath();
        char[] pass = "passUpaBroker".toCharArray();

        //get BROKER privkeys
        CAClient caClient = new CAClient();
        KeyStore ks = caClient.readKeyStoreFile(keyStorePath, pass); //KeyStorePath var do broker
        PrivateKey privateKey = (PrivateKey) ks.getKey("UpaBroker", pass); //keyStorePass="passUpaBroker"

        //Sign SOAPBody
        byte[] bodyBytes = SOAPElementToByteArray(sb);
        byte[] freshBytes = SOAPElementToByteArray(freshnessElement);
        byte[] allBytes = new byte[bodyBytes.length + freshBytes.length];

        System.arraycopy(bodyBytes, 0, allBytes, 0, bodyBytes.length);
        System.arraycopy(freshBytes, 0, allBytes, bodyBytes.length, freshBytes.length);

        byte[] msgDigSig = makeDigitalSignature(allBytes, privateKey);

        //get Base64.enconder
        BASE64Encoder encoder = new BASE64Encoder();
        String mSigStr = encoder.encode(msgDigSig);

        // add msigElement value
        // add MESSAGE SIGNATURE header element (name, namespace prefix, namespace)
        name = se.createName("MessageSignature", "mSig", "http://messageSignature");
        SOAPElement mSigElement = sh.addChildElement(name).addTextNode(mSigStr);

        //FIXME: CHANGE TO LOGS!!!
        //Print out the outbound SOAP message to System.out
        message.writeTo(System.out);
        System.out.println("");
    }

    public boolean handleInboundMessage(SOAPMessageContext smc) throws Exception{
        /*
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


            //header = hash;
            //body=m1;

        byte[] hashedBytes = sh.getValue().getBytes();

            //SOAPHeader sh = se.getHeader();//how to get url?
            //String url = "";
            //Key publicKey = CA.getCertificate(url).getPublicKey();

        return true;
        //return verifyDigitalSignature(hashedBytes,originalBytes,publicKey);
        */
        return true;
    }



    public byte[] getSOAPBodyContent(SOAPMessage sm) throws Exception{

        SOAPBody body = sm.getSOAPBody();

        //getting body string
        DOMSource source = new DOMSource(body);
        StringWriter stringResult = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
        String bodyString = stringResult.toString();
        byte[] soapBytes = bodyString.getBytes();
        return soapBytes;
    }

    private static byte[] SOAPElementToByteArray(SOAPElement elem) throws Exception {

        DOMSource source = new DOMSource(elem);
        StringWriter stringResult = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
        String message = stringResult.toString();
        byte[] soapBytes = message.getBytes();

        return soapBytes;
    }

    public static byte[] makeDigitalSignature(byte[] bytes,
                                              Key privateKey) throws Exception {

        // get a message digest object using the MD5 algorithm
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");

        // calculate the digest and print it out
        messageDigest.update(bytes);
        byte[] digest = messageDigest.digest();
        //System.out.println("Digest:");
        //System.out.println(printHexBinary(digest));

        // get an RSA cipher object
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        // encrypt the plaintext using the private key
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] cipherDigest = cipher.doFinal(digest);

        //System.out.println("Cipher digest:");
        //System.out.println(printHexBinary(cipherDigest));

        return cipherDigest;
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