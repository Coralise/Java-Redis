package me.wayne;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.wayne.daos.Commands;
import me.wayne.daos.Config;
import me.wayne.daos.Transaction;
import me.wayne.daos.commands.AbstractCommand;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.pubsub.Channel;
import me.wayne.daos.storevalues.StoreValue;

public class InMemoryStore implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(InMemoryStore.class.getName());
    // Private static instance of the same class
    private static InMemoryStore instance;

    static {
        LOGGER.log(Level.INFO, "Loading store...");
        if (Config.getInstance().isAofEnabled() && PersistenceManager.hasAofLog()) {
            LOGGER.log(Level.INFO, "AOF enabled, loading from AOF log...");
            instance = new InMemoryStore();
            PersistenceManager.loadAof();
        } else if (Config.getInstance().isSnapshotsEnabled() && PersistenceManager.hasSnapshot()) {
            LOGGER.log(Level.INFO, "Snapshots enabled, loading from snapshot data...");
            instance = PersistenceManager.loadStore();
        } else {
            LOGGER.log(Level.INFO, "No AOF or snapshot found or enabled, creating new store...");
            instance = new InMemoryStore();
        }
    }

    private final Map<String, StoreValue> store = new HashMap<>();
    private transient Map<Thread, Transaction> transactions = new HashMap<>();
    private transient Map<String, Channel> channels = new HashMap<>();

    InMemoryStore() {}

    public void executeCommand(String inputLine, StorePrintWriter out, @Nullable UUID requestUuid) {
        AbstractCommand<?> command = Commands.getCommand(inputLine.split(" ")[0]);
        if (command != null) {
            try {
                command.executeCommand(out, requestUuid, inputLine);
            } catch (AssertionError | Exception e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                out.println(requestUuid, e.getMessage());
                e.printStackTrace();
            }
        } else {
            out.println(requestUuid, "ERR Unknown Command");
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        transactions = new HashMap<>();
        channels = new HashMap<>();
    }

    // Public method to provide access to the instance
    public static synchronized InMemoryStore getInstance() {
        if (instance == null) {
            instance = new InMemoryStore();
        }
        return instance;
    }

    public Channel getChannel(String name) {
        return channels.get(name);
    }

    public Channel createOrGetChannel(String name) {
        return channels.computeIfAbsent(name, Channel::new);
    }

    public Channel removeChannel(String name) {
        return channels.remove(name);
    }

    public synchronized int executeTransaction(Thread thread, StorePrintWriter out, @Nullable UUID requestUuid) {
        Transaction transaction = transactions.get(thread);
        if (transaction == null) return -1;
        transaction.execute(out, requestUuid);
        return 1;
    }

    public void removeTransaction(Thread thread) {
        transactions.remove(thread);
    }

    public boolean createTransaction(Thread thread) {
        if (transactions.containsKey(thread)) return false;
        transactions.put(thread, new Transaction());
        return true;
    }

    public String addCommandToTransaction(Thread thread, String commandString) {
        Transaction transaction = transactions.get(thread);
        if (transaction == null) return "-ERR No transaction in progress";
        return transaction.addCommand(commandString);
    }

    public boolean hasTransaction(Thread thread) {
        return transactions.containsKey(thread);
    }

    public StoreValue getStoreValue(String key) {
        return getStoreValue(key, false);
    }

    public StoreValue getStoreValue(String key, boolean failWhenNotExist) {
        if (failWhenNotExist) {
            AssertUtil.assertTrue(store.containsKey(key), "Value does not exist for key: " + key);
        }
        return store.get(key);
    }

    public <T> T getStoreValue(String key, Class<T> clazz, boolean failWhenNotExist) {
        StoreValue storeValue = getStoreValue(key, failWhenNotExist);
        return storeValue.getValue(clazz);
    }

    @Nullable
    public <T> T getStoreValue(String key, Class<T> clazz) {
        StoreValue storeValue = getStoreValue(key);
        return storeValue == null ? null : storeValue.getValue(clazz);
    }

    @Nonnull
    public <T> T getStoreValue(String key, Class<T> clazz, T defaultValue) {
        StoreValue storeValue = getStoreValue(key);
        return storeValue == null ? defaultValue : storeValue.getValue(clazz);
    }

    public boolean hasStoreValue(String key) {
        return store.containsKey(key);
    }

    public StoreValue setStoreValue(String key, Serializable value, @Nullable String commandString) {
        if (commandString != null) PersistenceManager.appendToAof(commandString);
        PersistenceManager.snapshotSaveChecker(1);
        if (hasStoreValue(key)) {
            StoreValue oldStoreValue = getStoreValue(key);
            if (oldStoreValue.getValue().equals(value)) return oldStoreValue;
            if (oldStoreValue.hasExpiration()) oldStoreValue.stopExpirationThread();
        }
        return store.put(key, new StoreValue(value));
    }

    public StoreValue removeStoreValue(String key, @Nullable String commandString) {
        if (commandString != null) PersistenceManager.appendToAof(commandString);
        PersistenceManager.snapshotSaveChecker(1);
        return store.remove(key);
    }

    public Integer setExpiryInMillis(StoreValue storeValue, long l, boolean nx, boolean xx, boolean gt, boolean lt,
            String key, @Nullable String commandString) {
        if (commandString != null) PersistenceManager.appendToAof(commandString);
        return storeValue.setExpiryInMillis(l, nx, xx, gt, lt, key);
    }

    public Integer setExpiryAtDate(StoreValue storeValue, long timestamp, boolean nx, boolean xx, boolean gt,
            boolean lt, String key, @Nullable String commandString) {
        if (commandString != null) PersistenceManager.appendToAof(commandString);
        return storeValue.setExpiryAtDate(timestamp, nx, xx, gt, lt, key);
    }

}