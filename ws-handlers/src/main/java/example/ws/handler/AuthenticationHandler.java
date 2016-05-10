package example.ws.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.*;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.soap.MessageFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.*;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;


public class AuthenticationHandler implements SOAPHandler<SOAPMessageContext> {
    static private final Logger log = LogManager.getRootLogger();

    public static final String INVOKER_PROPERTY = "my.invoker.property";
    public static final String KSPATH_PROPERTY = "my.kspath.property";
    public static final String PASSWORD_PROPERTY = "my.password.property";

    public static long MaxWaitTime = 10;

    private static int ID_COUNTER = 0;

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        try {
            //sign outbound message
            if (outboundElement) handleOutboundMessage(smc);
            //verify signature
            else handleInboundMessage(smc);
        } catch (Exception e) {
            log.error(e);
        }
        return true;
    }

    public boolean handleFault(SOAPMessageContext smc) {
        //FIXME: handleMessage(SOAPMessageContext smc);
        return true;
    }

    public void close(MessageContext messageContext) {   }

    public void handleOutboundMessage(SOAPMessageContext smc) throws Exception{

        SOAPMessage message = smc.getMessage();
        SOAPPart sp = message.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPBody sb = se.getBody();

        SOAPHeader sh = se.getHeader();
        if (sh == null) sh = se.addHeader();
        String dateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()); //FIXME: xml date format

        Name name = se.createName("Freshness", "fresh", "http://freshness");
        SOAPElement freshnessElement = sh.addChildElement(name);

        name = se.createName("Identifier", "id", "http://identifier");
        freshnessElement.addChildElement(name).addTextNode(Integer.toString(ID_COUNTER++));

        name = se.createName("Date", "time", "http://date");
        freshnessElement.addChildElement(name).addTextNode(dateTime);

        String invoker = (String) smc.get(INVOKER_PROPERTY);
        String path = (String) smc.get(KSPATH_PROPERTY);
        String pass = (String) smc.get(PASSWORD_PROPERTY);



        System.out.println("INVOKER:" + invoker);
        System.out.println("KSPATH:" + path);
        System.out.println("PASS:" + pass);



        KeyStore ks = readKeyStoreFile(path, pass.toCharArray());
        PrivateKey privateKey = (PrivateKey) ks.getKey(invoker, pass.toCharArray());

        byte[] bodyBytes = SOAPElementToByteArray(sb);
        byte[] freshBytes = SOAPElementToByteArray(freshnessElement);
        byte[] allBytes = new byte[bodyBytes.length + freshBytes.length];

        System.arraycopy(bodyBytes, 0, allBytes, 0, bodyBytes.length);
        System.arraycopy(freshBytes, 0, allBytes, bodyBytes.length, freshBytes.length);

        byte[] msgDigSig = makeDigitalSignature(allBytes, privateKey);

        BASE64Encoder encoder = new BASE64Encoder();
        String mSigStr = encoder.encode(msgDigSig);

        name = se.createName("MessageSignature", "mSig", "http://messageSignature");
        sh.addChildElement(name).addTextNode(mSigStr);
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

        Name name = se.createName("assinatura", "assinatura", "http://demo");
        Iterator it = sh.getChildElements(name);
        // check header element
        if (!it.hasNext()) {
            System.out.println("Header element not found.");
            return true;
        }
        SOAPElement element = (SOAPElement) it.next();
        String timeInStringFormat = element.getValue();
        Time t = new Time(0);
        t.valueOf(timeInStringFormat);

        Date minutesAgo = new Date();
        minutesAgo.setTime(minutesAgo.getTime()-MaxWaitTime);
        if (t.before(minutesAgo)){
            return false;
        }



            byte[] hashedBytesCyphered = sh.getValue().getBytes();

            SOAPHeader sh = se.getHeader();//how to get url?
            String url = "";
            Key publicKey = CA.getCertificate(url).getPublicKey();

        return true;
        //return verifyDigitalSignature(hashedBytesCyphered,originalBytes,publicKey);
    */
        return true;
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
        // get an RSA cipher object
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        // encrypt the plaintext using the private key
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] cipherDigest = cipher.doFinal(digest);
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


    //FIXME - Exceptions
     /*-----------------------------------------------additional methods-----------------------------------------------*/


    /**
     * Reads a KeyStore from a file
     *
     * @return The read KeyStore
     * @throws Exception
     */
    public KeyStore readKeyStoreFile(String keyStoreFilePath, char[] keyStorePassword) throws Exception {
        FileInputStream fis;
        try {
            fis = new FileInputStream(keyStoreFilePath);
        } catch (FileNotFoundException e) {
            log.warn("Keystore file <" + keyStoreFilePath + "> not fount.");
            throw new Exception("Keystore file <" + keyStoreFilePath + "> not fount.");
        }
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(fis, keyStorePassword);

        return keystore;
    }

    /**
     * Reads a certificate from a file
     *
     * @return The read Certificate
     * @throws Exception
     */
    public Certificate readCertificateFile(String certificateFilePath) throws Exception {
        FileInputStream fis;

        try {
            fis = new FileInputStream(certificateFilePath);
        } catch (FileNotFoundException e) {
            log.warn("Certificate file <" + certificateFilePath + "> not fount.");
            throw new Exception("Certificate file <" + certificateFilePath + "> not fount.");
        }
        BufferedInputStream bis = new BufferedInputStream(fis);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        if (bis.available() > 0) {
            return cf.generateCertificate(bis);
        }

        bis.close();
        fis.close();
        log.warn("Nothing to read");
        throw new Exception("Nothing to read");
    }


    public Certificate toCertificate(byte[] certBytes) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return cf.generateCertificate(new ByteArrayInputStream(certBytes));
    }
}