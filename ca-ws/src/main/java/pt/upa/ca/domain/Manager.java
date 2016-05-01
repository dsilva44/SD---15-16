package pt.upa.ca.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.upa.ca.exception.CAException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyStore;

public class Manager {
    static private final Logger log = LogManager.getRootLogger();

    private static Manager manager = new Manager();
    private String wsName;
    private String keyStoreFilePath;
    private String keyStorePassword;

    private Manager() {}

    public void init(String wsName, String keyStoreFilePath) {
        this.wsName = wsName;
        this.keyStoreFilePath = keyStoreFilePath;
        keyStorePassword = "pass"+wsName;
    }

    public static Manager getInstance() { return manager; }

    public KeyStore readKeyStoreFile() throws Exception {
        FileInputStream fis;
        try {
            fis = new FileInputStream(keyStoreFilePath);
        } catch (FileNotFoundException e) {
            log.warn("Keystore file <" + keyStoreFilePath + "> not fount.");
            throw new CAException("Keystore file not found");
        }
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(fis, keyStorePassword.toCharArray());

        return keystore;
    }

}
