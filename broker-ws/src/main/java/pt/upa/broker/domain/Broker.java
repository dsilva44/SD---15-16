package pt.upa.broker.domain;

import java.util.ArrayList;
import java.util.List;

public abstract class Broker {
    private String primaryURL;
    private List<String> backupURLs;

    public Broker(String primaryURL) {
        this.primaryURL = primaryURL;
        backupURLs = new ArrayList<>();
    }

    public String getPrimaryURL() { return primaryURL; }
    public List<String> getBackupURLs() { return backupURLs; }

    public void addBackupURL(String url) {
        if (!backupURLs.contains(url))
            backupURLs.add(url);
    }

    public abstract void goNext();
    public abstract void monitor(long delay, long period);
}
