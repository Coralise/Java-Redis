package me.wayne;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

public class InMemoryStore {

    private final Map<String, Object> store = new HashMap<>();

    // Key: Sorted set key, Value: Map of member to score
    private final Map<String, Map<String, Integer>> treeSetMembers = new HashMap<>();

    public Map<String, Object> getStore() {
        return store;
    }

    public Map<String, Map<String, Integer>> getTreeSetMembers() {
        return treeSetMembers;
    }

    // public void setJson(String key, JSONObject value) {
    //     store.put(key, value);
    // }

    // public JSONObject getJson(String key) {
    //     AssertUtil.assertTrue(store.containsKey(key), KEY_DOESNT_EXIST_MSG);
    //     AssertUtil.assertTrue(store.get(key) instanceof JSONObject, NON_JSON_ERROR_MSG);
    //     return (JSONObject) store.get(key);
    // }

    // public void delJson(String key) {
    //     AssertUtil.assertTrue(store.containsKey(key), KEY_DOESNT_EXIST_MSG);
    //     AssertUtil.assertTrue(store.get(key) instanceof JSONObject, NON_JSON_ERROR_MSG);
    //     store.remove(key);
    // }

    // public void jsonArrAppend(String key, Object value) {
    //     AssertUtil.assertTrue(store.containsKey(key), KEY_DOESNT_EXIST_MSG);
    //     AssertUtil.assertTrue(store.get(key) instanceof JSONArray, NON_JSON_ERROR_MSG);
    //     JSONArray jsonArray = (JSONArray) store.get(key);
    //     jsonArray.put(value);
    //     store.put(key, jsonArray);
    // }

}