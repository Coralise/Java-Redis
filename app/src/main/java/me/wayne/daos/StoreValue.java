package me.wayne.daos;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import me.wayne.InMemoryStore;


public class StoreValue {

    private static final Logger LOGGER = Logger.getLogger(StoreValue.class.getName());

    @Nonnull
    private final Object value;

    // First: Unix timestamp in milliseconds, Second: Delete thread
    Pair<Long, Thread> expiryThread;

    public StoreValue(@Nonnull Object value) {
        this.value = value;
        this.expiryThread = null;
    }

    public boolean hasExpiration() {
        return expiryThread != null;
    }

    public boolean stopExpirationThread() {
        if (expiryThread == null) return false;
        expiryThread.getSecond().interrupt();
        return true;
    }

    public boolean startExpirationThread(String keyToDelete) {
        if (expiryThread == null) return false;
        if (expiryThread.getFirst() < System.currentTimeMillis()) {
            InMemoryStore.getInstance().removeStoreValue(keyToDelete);
            return true;
        }
        expiryThread.getSecond().start();
        return true;
    }

    public int getTimeToLive() {
        if (expiryThread == null) return -1;
        long currentTimestamp = System.currentTimeMillis();
        long timeInMillis = expiryThread.getFirst() - currentTimestamp;
        return (int) Math.ceil((double) timeInMillis / 1000);
    }

    public int setExpiryInMillis(Long timeInMillis, boolean nx, boolean xx, boolean gt, boolean lt, String key) {
        long currentTimestamp = System.currentTimeMillis();
        long timestamp = currentTimestamp + timeInMillis;
        return setExpiryAtDate(timestamp, nx, xx, gt, lt, key);
    }

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
            if (gt && expiryThread.getFirst() > timestamp) return 0;
            if (lt && expiryThread.getFirst() < timestamp) return 0;
            expiryThread.getSecond().interrupt();
        }
        expiryThread = new Pair<>(timestamp, new Thread(() -> {
            try {
                Thread.sleep(timeInMillis);
            } catch (InterruptedException e) {
                return;
            }
            InMemoryStore.getInstance().removeStoreValue(key);
            LOGGER.log(Level.INFO, "Key {0} has expired", key);
        }));
        expiryThread.getSecond().start();
        return 1;
    }

    @Nonnull
    public <T> T getValue(Class<T> clazz) {
        if (!clazz.isInstance(value)) {
            throw new IllegalArgumentException("Value is not of the expected type (" + clazz.getSimpleName() + ")");
        }
        return clazz.cast(value);
    }

    public Object getValue() {
        return value;
    }

    public boolean isInstanceOfClass(Class<?> clazz) {
        return clazz.isInstance(value);
    }
    
}
