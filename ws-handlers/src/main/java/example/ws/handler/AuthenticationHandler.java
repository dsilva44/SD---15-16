package example.ws.handler;

import example.ws.exception.AuthSecurityException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.ca.ws.cli.CAClient;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.*;
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

    private static HashMap<String,Integer> id_pairs= new HashMap<String,Integer>(){{
        put("UpaBroker",0);
        put("UpaTransporter1",0);
        put("UpaTransporter2",0);
        put("ToSend",0);
    }};

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        try {
            //sign outbound message
            if (outboundElement)
                handleOutboundMessage(smc);
            //verify signature
            else handleInboundMessage(smc);
        } catch (AuthSecurityException e) {
            throw new RuntimeException("Security Error", e.getCause());
        } catch (Exception e) {
            log.error(e);
        }
        return true;
    }

    public boolean handleFault(SOAPMessageContext smc) {
        return true;
    }

    public void close(MessageContext messageContext) {   }

    public void handleOutboundMessage(SOAPMessageContext smc)
            throws Exception {

        String invoker = (String) smc.get(INVOKER_PROPERTY);
        String path = (String) smc.get(KSPATH_PROPERTY);
        String pass = (String) smc.get(PASSWORD_PROPERTY);

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
        SOAPElement idElement = freshnessElement.addChildElement(name);
        Integer id = id_pairs.get("ToSend");
        idElement.addTextNode(Integer.toString(id));
        id_pairs.put("ToSend", id_pairs.get("ToSend") + 1);

        name = se.createName("Date", "time", "http://date");
        SOAPElement dateElement = freshnessElement.addChildElement(name);
        dateElement.addTextNode(dateTime);

        name = se.createName("SenderName", "sname", "http://senderName");
        SOAPElement senderElement = sh.addChildElement(name);
        senderElement.addTextNode(invoker);

        KeyStore ks = readKeyStoreFile(path, pass.toCharArray());
        PrivateKey privateKey = (PrivateKey) ks.getKey(invoker, pass.toCharArray());

        byte[] bodyBytes = SOAPElementToByteArray(sb);
        byte[] dateBytes = dateElement.getValue().getBytes("UTF-8");
        byte[] idBytes = idElement.getValue().getBytes("UTF-8");
        byte[] senderBytes = senderElement.getValue(). getBytes("UTF-8");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(bodyBytes); baos.write(dateBytes); baos.write(idBytes); baos.write(senderBytes);
        byte[] allBytes = baos.toByteArray();

        byte[] msgDigSig = makeDigitalSignature(allBytes, privateKey);
        String mSigStr = DatatypeConverter.printBase64Binary(msgDigSig);

        name = se.createName("MessageSignature", "mSig", "http://messageSignature");
        sh.addChildElement(name).addTextNode(mSigStr);
    }

    public void handleInboundMessage(SOAPMessageContext smc) throws Exception{

        SOAPMessage msg = smc.getMessage();
        SOAPPart sp = msg.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPBody sb = se.getBody();
        SOAPHeader sh = se.getHeader();

        //get hashed bytes from <MessageSignature> element
        SOAPElement element = getElement(se,"MessageSignature", "mSig", "http://messageSignature");
        String strReceived = element.getValue();
        byte[] hashedBytes = DatatypeConverter.parseBase64Binary(strReceived);

        //SOAPElement freshElement= getElement(se,"Freshness", "fresh", "http://freshness");

        Name name = se.createName("Freshness", "fresh", "http://freshness");
        SOAPElement freshElement = (SOAPElement) sh.getChildElements(name).next();

        name = se.createName("Identifier", "id", "http://identifier");
        SOAPElement idElement = (SOAPElement) freshElement.getChildElements(name).next();

        name = se.createName("Date", "time", "http://date");
        SOAPElement dateElement = (SOAPElement) freshElement.getChildElements(name).next();

        name = se.createName("SenderName", "sname", "http://senderName");
        SOAPElement senderElement = (SOAPElement) sh.getChildElements(name).next();

        List<SOAPElement> elementsInFresh= getElements(freshElement);
        SOAPElement elementId = elementsInFresh.get(0);
        SOAPElement elementDate = elementsInFresh.get(1);

        byte[] bodyBytes = SOAPElementToByteArray(sb);
        byte[] dateBytes = dateElement.getValue().getBytes("UTF-8");
        byte[] idBytes = idElement.getValue().getBytes("UTF-8");
        byte[] senderBytes = senderElement.getValue(). getBytes("UTF-8");



        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(bodyBytes); baos.write(dateBytes); baos.write(idBytes); baos.write(senderBytes);
        byte[] allBytes = baos.toByteArray();

        CAClient client = new CAClient("http://localhost:9090");
        byte[] certBytes = client.requestCertificateFile(senderElement.getValue());
        Certificate senderCer = toCertificate(certBytes);

        Certificate caCer = readCertificateFile("UpaCA.cer");
        if (caCer == null) return;
        senderCer.verify(caCer.getPublicKey());

        PublicKey publicKeySender = senderCer.getPublicKey();

        if (!verifyDigitalSignature(hashedBytes, allBytes, publicKeySender))
            throw new AuthSecurityException("Security error");
    }

    /*-----------------------------------------------additional methods-----------------------------------------------*/

    protected void isValidCertDate(Certificate cert) throws CertificateNotYetValidException, CertificateExpiredException {
        ((X509Certificate) cert).checkValidity();
    }

    protected PublicKey getCAPublicKey(SOAPMessageContext smc) throws Exception {
        //fixme uninitialized keystore
        /*KeyStore ks = KeyStore.getInstance("JKS");
        Certificate certCA = ks.getCertificate("UpaCA");
        */
        Certificate certCA = readCertificateFile("/home/dziergwa/Desktop/SD/T_27-project/transporter-ws/src/main/resources/UpaCA.cer");
        return certCA.getPublicKey();
    }
    protected byte[] joinElementsInBytes(SOAPElement elemBody,SOAPElement elemFresh) throws TransformerException {

        byte[] bodyBytes = SOAPElementToByteArray(elemBody);
        byte[] freshBytes = SOAPElementToByteArray(elemFresh);
        byte[] allBytes = new byte[bodyBytes.length + freshBytes.length];

        System.arraycopy(bodyBytes, 0, allBytes, 0, bodyBytes.length);
        System.arraycopy(freshBytes, 0, allBytes, bodyBytes.length, freshBytes.length);

        return allBytes;

    }


    protected List<SOAPElement> getElements(SOAPElement elem) {
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
            log.warn(a+" Not found");
            return null;
        }
        return (SOAPElement) iterator.next();
    }


    protected boolean checkTimestamp(String timeInStringFormat) throws ParseException {

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = format.parse(timeInStringFormat);

        Date limit = new Date();
        limit.setTime(limit.getTime()-MaxWaitTimeInMs);

        return !date.before(limit);
    }


    protected boolean checkIdentifier(String stringId, String invoker){

        int id = Integer.parseInt(stringId);
        int expected = id_pairs.get(invoker);
        if (id < expected){
            return false;
        }
        else{
            id_pairs.put(invoker, id + 1);
        }
        return true;
    }

    private static byte[] SOAPElementToByteArray(SOAPElement elem) throws TransformerException {

        DOMSource source = new DOMSource(elem);
        StringWriter stringResult = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
        String message = stringResult.toString();

        return DatatypeConverter.parseBase64Binary(message);
    }

    /** auxiliary method to calculate digest from text and cipher it */
    public static byte[] makeDigitalSignature(byte[] bytes, PrivateKey privateKey) throws Exception {

        // get a signature object using the SHA-1 and RSA combo
        // and sign the plaintext with the private key
        Signature sig = Signature.getInstance("SHA1WithRSA");
        sig.initSign(privateKey);
        sig.update(bytes);
        byte[] signature = sig.sign();

        return signature;
    }

    /**
     * auxiliary method to calculate new digest from text and compare it to the
     * to deciphered digest
     */
    public static boolean verifyDigitalSignature(byte[] cipherDigest, byte[] bytes, PublicKey publicKey) throws Exception {

        // verify the signature with the public key
        Signature sig = Signature.getInstance("SHA1WithRSA");
        sig.initVerify(publicKey);
        sig.update(bytes);
        try {
            return sig.verify(cipherDigest);
        } catch (SignatureException se) {
            System.err.println("Caught exception while verifying " + se);
            return false;
        }
    }


    public KeyStore readKeyStoreFile(String keyStoreFilePath, char[] keyStorePassword)  {
        ClassLoader classLoader = getClass().getClassLoader();

        KeyStore keystore = null;
        try {
            URL url = classLoader.getResource(keyStoreFilePath);
            if (url == null)
                return null;
            FileInputStream fis = new FileInputStream(url.getFile());
            keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(fis, keyStorePassword);

            return keystore;
        } catch (FileNotFoundException e) {
            log.debug("FileInputStream", e);
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            e.printStackTrace();
        }
        return keystore;
    }

    private Certificate readCertificateFile(String certificateFilePath) {
        ClassLoader classLoader = getClass().getClassLoader();

        try{
            URL url = classLoader.getResource(certificateFilePath);
            if(url==null)
                return null;
            FileInputStream fis = new FileInputStream(url.getFile());
            BufferedInputStream bis = new BufferedInputStream(fis);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            if (bis.available() > 0)
                return cf.generateCertificate(bis);

            bis.close();
            fis.close();
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    public Certificate toCertificate(byte[] certBytes) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return cf.generateCertificate(new ByteArrayInputStream(certBytes));
    }
}