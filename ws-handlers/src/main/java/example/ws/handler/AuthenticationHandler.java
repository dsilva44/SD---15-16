package example.ws.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.ca.ws.cli.CAClient;
import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
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

    private static HashMap<String,Integer> id_pairs_to_send= new HashMap<String,Integer>(){{
        put("UpaBroker",0);
        put("UpaTransporter1",0);
        put("UpaTransporter2",0);
    }};


    private static HashMap<String,Integer> id_pairs_Expected= new HashMap<String,Integer>(){{
        put("UpaBroker",0);
        put("UpaTransporter1",0);
        put("UpaTransporter2",0);
    }};

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
        } catch (Exception e) { //FIXME - todas as mensagens vao ser apanhadas, secalhar não é isso que queremos !!!
            log.warn(e.getMessage());
        }
        return true;
    }

    public boolean handleFault(SOAPMessageContext smc) {
        return true;
    }

    public void close(MessageContext messageContext) {   }

    public void handleOutboundMessage(SOAPMessageContext smc) throws Exception{

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
        //Integer id = id_pairs_to_send.get(destination);
        freshnessElement.addChildElement(name).addTextNode(Long.toString(ID_COUNTER++));
        //id_pairs_to_send.put(invoker, id_pairs_to_send.get(invoker) + 1);

        name = se.createName("Date", "time", "http://date");
        freshnessElement.addChildElement(name).addTextNode(dateTime);

        name = se.createName("SenderName", "sname", "http://senderName");
        sh.addChildElement(name).addTextNode(invoker);

        //FIXME - This may generate null pointer exception
        KeyStore ks = readKeyStoreFile(path, pass.toCharArray());
        PrivateKey privateKey = (PrivateKey) ks.getKey(invoker, pass.toCharArray());

        byte[] bodyBytes = SOAPElementToByteArray(sb);
        byte[] freshBytes = SOAPElementToByteArray(freshnessElement);
        byte[] allBytes = new byte[bodyBytes.length + freshBytes.length];

        System.arraycopy(bodyBytes, 0, allBytes, 0, bodyBytes.length);
        System.arraycopy(freshBytes, 0, allBytes, bodyBytes.length, freshBytes.length);

        byte[] msgDigSig = makeDigitalSignature(allBytes, privateKey);

        String mSigStr = DatatypeConverter.printBase64Binary(msgDigSig);

        name = se.createName("MessageSignature", "mSig", "http://messageSignature");
        sh.addChildElement(name).addTextNode(mSigStr);
    }

    public void handleInboundMessage(SOAPMessageContext smc) throws Exception{


        SOAPMessage msg = smc.getMessage();
        SOAPPart sp = msg.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();

        //get hashed bytes from <MessageSignature> element
        SOAPElement element = getElement(se,"MessageSignature", "mSig", "http://messageSignature");
        String strReceived = element.getValue().trim();
        byte[] hashedBytes = DatatypeConverter.parseBase64Binary(strReceived);

        SOAPElement elementFresh = getElement(se,"Freshness", "fresh", "http://freshness");
        SOAPElement elementName = getElement(se,"SenderName", "sname", "http://senderName");
        byte[] allBytes = joinElementsInBytes(se.getBody(),elementFresh,elementName);

        List<SOAPElement> elementsInFresh= getElements(elementFresh);

        SOAPElement elementId = elementsInFresh.get(0);
        SOAPElement elementDate = elementsInFresh.get(1);

        //fixme dinamically
        PublicKey CAPublicKey = getCAPublicKey(elementName);

        //get INVOKER certificate
        String invoker = (String) smc.get(INVOKER_PROPERTY);
        CAClient caClient = new CAClient("http://localhost:9090");
        byte[] certificateEncoded = caClient.requestCertificateFile(invoker);
        Certificate cert = toCertificate(certificateEncoded);

        try{
            cert.verify(CAPublicKey);
        }
        catch(Exception e){
            log.warn("Verify certificate failed");
            throw new Exception();
        }

        try {
            isValidCertDate(cert);
        }
        catch(CertificateNotYetValidException| CertificateExpiredException c){
            log.warn("Invalid certificate date");
            throw new Exception();
        }


        PublicKey publicKey = cert.getPublicKey();
        Boolean isValidSignature = verifyDigitalSignature(hashedBytes,allBytes,publicKey);
        if(!isValidSignature) {
            log.warn("Invalid signature");
            throw new Exception();
        }

        String stringId = elementId.getValue();
        String destination = elementName.getValue();
        Boolean isValidIdentifier = checkIdentifier(stringId,invoker);
        if (!isValidIdentifier){
            log.warn("Invalid identifier");
            throw new Exception();
        }

        String timeInStringFormat = elementDate.getValue();
        Boolean isValidTimestamp = checkTimestamp(timeInStringFormat);
        if(!isValidTimestamp){
            log.warn("Invalid date");
            throw new Exception();
        }
        //FIXME systemOuts
        System.out.println("ALLVALID");
        log.warn("ALL VALID");

    }

    protected void isValidCertDate(Certificate cert) throws CertificateNotYetValidException, CertificateExpiredException {
        ((X509Certificate) cert).checkValidity();
    }

    protected PublicKey getCAPublicKey(SOAPElement elem) throws Exception {

        String destinationName= elem.getValue();
        //String destinationName = "UpaBroker";
        String begin = "../../T_27-project/";
        String pasta = null;

        switch(destinationName){
            case "UpaTransporter1": pasta = "transporter-ws";
                break;
            case "UpaTransporter2": pasta = "transporter-ws";
                break;
            case "UpaBroker": pasta = "broker-ws";
                break;
        }

        String path = begin + pasta +"/src/main/resources/"+destinationName+".jks";
        String pass = "pass"+destinationName;
        KeyStore ks = readKeyStoreFile(path, pass.toCharArray());
        Certificate certCA = ks.getCertificate("UpaCA");
        return certCA.getPublicKey();
    }
    protected byte[] joinElementsInBytes(SOAPElement elemBody,SOAPElement elemFresh, SOAPElement elemName) throws Exception{

        byte[] bodyBytes = SOAPElementToByteArray(elemBody);
        byte[] freshBytes = SOAPElementToByteArray(elemFresh);
        byte[] nameBytes = SOAPElementToByteArray(elemName);
        byte[] allBytes = new byte[bodyBytes.length + freshBytes.length + nameBytes.length];

        System.arraycopy(bodyBytes, 0, allBytes, 0, bodyBytes.length);
        System.arraycopy(freshBytes, 0, allBytes, bodyBytes.length, freshBytes.length);
        System.arraycopy(nameBytes,0,allBytes,bodyBytes.length+freshBytes.length,nameBytes.length);
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
        if (date.before(limit)){
            return false;
        }
        return true;
    }

    protected boolean checkIdentifier(String stringId, String invoker){

        int id = Integer.parseInt(stringId);

        Integer id_expected
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
                                              Key privateKey){
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(bytes);
            byte[] digest = messageDigest.digest();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] cipherDigest = cipher.doFinal(digest);
            return cipherDigest;
        }catch(Exception e){
            throw new SecurityException("Security Error!");
        }
    }

    public static boolean verifyDigitalSignature(byte[] cipherDigest,
                                                 byte[] text,
                                                 Key publicKey) throws Exception {

        try {
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

            for (int i = 0; i < digest.length; i++)
                if (digest[i] != decipheredDigest[i])
                    return false;
            return true;
        }
        catch(Exception e){
            throw new SecurityException("Verify Signature Error");
        }
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
            throw new Exception("Keystore file <" + keyStoreFilePath + "> not fount."); // FIXME Change exception
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
            throw new Exception("Certificate file <" + certificateFilePath + "> not fount."); // FIXME Change exception
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