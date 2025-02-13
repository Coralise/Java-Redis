package me.wayne.daos;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.wayne.InMemoryStore;

public class Expiry implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(Expiry.class.getName());

    private final long timestamp;
    private final String keyToDelete;
    private final transient Thread deletionThread;

    @SuppressWarnings("all")
    public Expiry(long timestamp, String keyToDelete) {
        this.timestamp = timestamp;
        this.keyToDelete = keyToDelete;
        deletionThread = new Thread(() -> {
            long timeInMillis = timestamp - System.currentTimeMillis();
            if (timeInMillis > 0) {
                try {
                    Thread.sleep(timeInMillis);
                } catch (InterruptedException e) {
                    return;
                }
            }
            InMemoryStore.getInstance().removeStoreValue(keyToDelete);
            LOGGER.log(Level.INFO, "Key {0} has expired", keyToDelete);
        });
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getKeyToDelete() {
        return keyToDelete;
    }

    public void stop() {
        if (deletionThread.isAlive())
            deletionThread.interrupt();
    }

    public void start() {
        if (!deletionThread.isAlive())
            deletionThread.start();
    }

    public void restart() {
        stop();
        start();
    }
    
}
