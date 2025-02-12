package me.wayne;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.wayne.daos.StoreValue;

public class InMemoryStore {

    // Private static instance of the same class
    private static InMemoryStore instance;

    // Private constructor to prevent instantiation
    private InMemoryStore() {}

    // Public method to provide access to the instance
    public static synchronized InMemoryStore getInstance() {
        if (instance == null) {
            instance = new InMemoryStore();
        }
        return instance;
    }

    private final Map<String, StoreValue> store = new HashMap<>();

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

    public StoreValue setStoreValue(String key, Object value) {
        if (hasStoreValue(key)) {
            StoreValue oldStoreValue = getStoreValue(key);
            if (oldStoreValue.getValue().equals(value)) return oldStoreValue;
            if (oldStoreValue.hasExpiration()) oldStoreValue.stopExpirationThread();
        }
        return store.put(key, new StoreValue(value));
    }

    public StoreValue removeStoreValue(String key) {
        return store.remove(key);
    }

}