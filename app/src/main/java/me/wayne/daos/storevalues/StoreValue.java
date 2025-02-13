package me.wayne.daos.storevalues;

import java.io.Serializable;

import javax.annotation.Nonnull;

import me.wayne.InMemoryStore;
import me.wayne.daos.Expiry;


public class StoreValue implements Serializable {

    @Nonnull
    private final Serializable value;

    // First: Unix timestamp in milliseconds, Second: Delete thread
    Expiry expiryThread;

    public StoreValue(@Nonnull Serializable value) {
        this.value = value;
        this.expiryThread = null;
    }

    public boolean hasExpiration() {
        return expiryThread != null;
    }

    public boolean stopExpirationThread() {
        if (expiryThread == null) return false;
        expiryThread.stop();
        return true;
    }

    public boolean startExpirationThread() {
        if (expiryThread == null) return false;
        expiryThread.start();
        return true;
    }

    public int getTimeToLive() {
        if (expiryThread == null) return -1;
        long currentTimestamp = System.currentTimeMillis();
        long timeInMillis = expiryThread.getTimestamp() - currentTimestamp;
        return (int) Math.ceil((double) timeInMillis / 1000);
    }

    public int setExpiryInMillis(Long timeInMillis, boolean nx, boolean xx, boolean gt, boolean lt, String key) {
        long currentTimestamp = System.currentTimeMillis();
        long timestamp = currentTimestamp + timeInMillis;
        return setExpiryAtDate(timestamp, nx, xx, gt, lt, key);
    }

    @SuppressWarnings("all")
    public int setExpiryAtDate(Long timestamp, boolean nx, boolean xx, boolean gt, boolean lt, String key) {
        long currentTimestamp = System.currentTimeMillis();
        long timeInMillis = timestamp - currentTimestamp;
        if (timeInMillis <= 0) {
            InMemoryStore.getInstance().removeStoreValue(key);
        }
        if (nx && xx) throw new IllegalArgumentException("NX and XX are mutually exclusive");
        if (gt && lt) throw new IllegalArgumentException("GT and LT are mutually exclusive");
        if (nx && expiryThread != null) return 0;
        if (xx && expiryThread == null) return 0;
        if (expiryThread != null) {
            if (gt && expiryThread.getTimestamp() > timestamp) return 0;
            if (lt && expiryThread.getTimestamp() < timestamp) return 0;
            expiryThread.stop();
        }
        expiryThread = new Expiry(timestamp, key);
        expiryThread.start();
        return 1;
    }

    @Nonnull
    public <T> T getValue(Class<T> clazz) {
        if (!clazz.isInstance(value)) {
            throw new IllegalArgumentException("Value is not of the expected type (" + clazz.getSimpleName() + ")");
        }
        return clazz.cast(value);
    }

    public Serializable getValue() {
        return value;
    }

    public boolean isInstanceOfClass(Class<?> clazz) {
        return clazz.isInstance(value);
    }
    
}
