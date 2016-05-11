package example.ws.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.*;
import pt.upa.ca.ws.cli.CAClient;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
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
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class AuthenticationHandler implements SOAPHandler<SOAPMessageContext> {
    static private final Logger log = LogManager.getRootLogger();

    public static final String INVOKER_PROPERTY = "my.invoker.property";
    public static final String KSPATH_PROPERTY = "my.kspath.property";
    public static final String PASSWORD_PROPERTY = "my.password.property";

    private static long MaxWaitTimeInMs = 1*1000*60;  //1 minute margin


    private static int ID_COUNTER_EXPECTED = 0;

    private static long ID_COUNTER = 0;

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
        String dateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());

        Name name = se.createName("Freshness", "fresh", "http://freshness");
        SOAPElement freshnessElement = sh.addChildElement(name);

        name = se.createName("Identifier", "id", "http://identifier");
        freshnessElement.addChildElement(name).addTextNode(Long.toString(ID_COUNTER++));

        name = se.createName("Date", "time", "http://date");
        freshnessElement.addChildElement(name).addTextNode(dateTime);

        String invoker = (String) smc.get(INVOKER_PROPERTY);
        String path = (String) smc.get(KSPATH_PROPERTY);
        String pass = (String) smc.get(PASSWORD_PROPERTY);

        KeyStore ks = readKeyStoreFile(path, pass.toCharArray());
        PrivateKey privateKey = (PrivateKey) ks.getKey(invoker, pass.toCharArray());

        byte[] bodyBytes = SOAPElementToByteArray(sb);
        byte[] freshBytes = SOAPElementToByteArray(freshnessElement);
        byte[] allBytes = new byte[bodyBytes.length + freshBytes.length];

        System.arraycopy(bodyBytes, 0, allBytes, 0, bodyBytes.length);
        System.arraycopy(freshBytes, 0, allBytes, bodyBytes.length, freshBytes.length);

        byte[] msgDigSig = makeDigitalSignature(allBytes, privateKey);

        System.out.println("BytesBEFOREencode:"+msgDigSig+"\n");

        //String mSigStr = Base64.getEncoder().encodeToString(msgDigSig);
        //String mSigStr = DatatypeConverter.printBase64Binary(msgDigSig);
        //String mSigStr = printBase64Binary(msgDigSig);
        BASE64Encoder encoder = new BASE64Encoder();
        String mSigStr = encoder.encode(msgDigSig);
        //mSigStr.trim();
        System.out.println("StringSENT:"+mSigStr+"\n");
        System.out.println("StringSENTSIZE:"+mSigStr.length()+"\n");



        name = se.createName("MessageSignature", "mSig", "http://messageSignature");
        sh.addChildElement(name).addTextNode(mSigStr);
    }

    public void handleInboundMessage(SOAPMessageContext smc) throws Exception{

        //FIXME systemOuts
        SOAPMessage msg = smc.getMessage();
        SOAPPart sp = msg.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();

        //get hashed bytes from <MessageSignature> element
        SOAPElement element = getElement(se,"MessageSignature", "mSig", "http://messageSignature");
        String strReceived = element.getValue().trim();

        System.out.println("StringReceived:"+strReceived+"\n");
        System.out.println("StringReceivedSize:"+strReceived.length()+"\n");
        //byte[] hashedBytes = Base64.getDecoder().decode(strReceived);
        //byte[] hashedBytes = DatatypeConverter.parseBase64Binary(strReceived);
        //byte[] hashedBytes = parseBase64Binary(strReceived);
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] hashedBytes = decoder.decodeBuffer(strReceived);
        System.out.println("BytesAFTERDecode:"+hashedBytes+"\n");

        SOAPElement elementFresh = getElement(se,"Freshness", "fresh", "http://freshness");

        byte[] allBytes = joinElementsInBytes(se.getBody(),elementFresh);

        List<SOAPElement> elementsInFresh= getElements(elementFresh);

        SOAPElement elementId = elementsInFresh.get(0);
        SOAPElement elementDate = elementsInFresh.get(1);

        PublicKey CAPublicKey = getCAPublicKey(smc);

        //get INVOKER certificate
        String invoker = (String) smc.get(INVOKER_PROPERTY);
        CAClient caClient = new CAClient("http://localhost:9090");
        byte[] certificateEncoded = caClient.requestCertificateFile(invoker);
        System.out.println("never give up");
        Certificate cert = caClient.toCertificate(certificateEncoded);

        try{
            cert.verify(CAPublicKey);
        }
        catch(Exception e){
            log.warn("Verify certificate failed");
            return;
        }

        Date certLimitDate = ((X509Certificate) cert).getNotBefore();
        Date now = new Date();

        if (now.after(certLimitDate)){
            log.warn("invalid certificate date");
            return;
        }
        //fixme testar getCApublicKey

        PublicKey publicKey = cert.getPublicKey();
/*
        Boolean isValidSignature = verifyDigitalSignature(hashedBytes,allBytes,publicKey);
        System.out.println("verify didnt blow up");
        if(!isValidSignature) {
            System.out.println("Digital signature is NOT VALID!");
            return;
        }
*/

        Boolean isValidIdentifier = checkIdentifier(elementId);
        if (!isValidIdentifier){
            return;
        }

        Boolean isValidTimestamp = checkTimestamp(elementDate);
        if(!isValidTimestamp){
            return;
        }

        System.out.println("ALL VALID");

    }


    private PublicKey getCAPublicKey(SOAPMessageContext smc) throws Exception {
        //fixme uninitialized keystore
        /*KeyStore ks = KeyStore.getInstance("JKS");
        Certificate certCA = ks.getCertificate("UpaCA");
        */
        Certificate certCA = readCertificateFile("/home/dziergwa/Desktop/SD/T_27-project/transporter-ws/src/main/resources/UpaCA.cer");
        return certCA.getPublicKey();
    }
    private byte[] joinElementsInBytes(SOAPElement elemBody,SOAPElement elemFresh) throws Exception{

        byte[] bodyBytes = SOAPElementToByteArray(elemBody);
        byte[] freshBytes = SOAPElementToByteArray(elemFresh);
        byte[] allBytes = new byte[bodyBytes.length + freshBytes.length];

        System.arraycopy(bodyBytes, 0, allBytes, 0, bodyBytes.length);
        System.arraycopy(freshBytes, 0, allBytes, bodyBytes.length, freshBytes.length);

        return allBytes;

    }


    protected List<SOAPElement> getElements(SOAPElement elem) throws Exception{
        List<SOAPElement> list = new ArrayList();

        Iterator iterator = elem.getChildElements();

        while (iterator.hasNext()) {
            SOAPElement newElem = (SOAPElement)iterator.next();
            list.add(newElem);
        }
        return list;
    }

    protected SOAPElement getElement(SOAPEnvelope se,String a, String b, String c) throws Exception{
        Name name = se.createName(a, b, c);
        Iterator iterator = se.getHeader().getChildElements(name);

        if (!iterator.hasNext()) {
            return null;
        }
        return (SOAPElement) iterator.next();
    }


    protected boolean checkTimestamp(SOAPElement elem) throws ParseException {
        String timeInStringFormat = elem.getValue();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = format.parse(timeInStringFormat);

        Date limit = new Date();
        limit.setTime(limit.getTime()-MaxWaitTimeInMs);
        if (date.before(limit)){
            return false;
        }
        return true;
    }

    protected boolean checkIdentifier(SOAPElement elem){
        String stringId = elem.getValue();
        int id = Integer.parseInt(stringId);

        if (id != ID_COUNTER_EXPECTED){
            return false;
        }
        else{
            ID_COUNTER_EXPECTED++;
        }
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

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        messageDigest.update(bytes);
        byte[] digest = messageDigest.digest();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
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