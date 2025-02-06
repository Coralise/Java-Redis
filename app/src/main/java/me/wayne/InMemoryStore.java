package me.wayne;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

class InMemoryStore {
    private final Map<String, Object> store = new HashMap<>();
    private String nonIntegerErrorMsg = "Value is not of type Integer";
    private String nonStringErrorMsg = "Value is not of type String";
    private String keyDoesntExistMsg = "Key does not exist";
    private String nonListErrorMsg = "Value is not of type List";
    private String nonJsonErrorMsg = "Value is not of type JSONObject or JSONArray";
    
    public void set(String key, Object value) {
        store.put(key, value);
    }

    public Object get(String key) {
        return store.get(key);
    }

    public Object delete(String key) {
        return store.remove(key);
    }

    public void append(String key, Object value) {
        AssertUtil.assertTrue(store.containsKey(key), keyDoesntExistMsg);
        AssertUtil.assertTrue(store.get(key) instanceof CharSequence, "Existing value is not of type CharSequence");
        AssertUtil.assertTrue(value instanceof CharSequence, "Value to append is not of type CharSequence");
        store.put(key, store.get(key) + value.toString());
    }

    public int strLen(String key) {
        AssertUtil.assertTrue(store.containsKey(key), keyDoesntExistMsg);
        AssertUtil.assertTrue(store.get(key) instanceof CharSequence, "Value is not of type CharSequence");
        return ((CharSequence) store.get(key)).length();
    }

    public String getRange(String key, int start, int end) {
        AssertUtil.assertTrue(store.containsKey(key), keyDoesntExistMsg);
        AssertUtil.assertTrue(store.get(key) instanceof String, nonStringErrorMsg);
        String value = (String) store.get(key);
        return value.substring(start, end + 1);
    }

    public String setRange(String key, int offset, String value) {
        AssertUtil.assertTrue(store.containsKey(key), keyDoesntExistMsg);
        AssertUtil.assertTrue(store.get(key) instanceof String, nonStringErrorMsg);
        String existingValue = (String) store.get(key);
        StringBuilder newValue = new StringBuilder(existingValue);
        newValue.replace(offset, offset + value.length(), value);
        store.put(key, newValue.toString());
        return newValue.toString();
    }

    public void incr(String key) {
        AssertUtil.assertTrue(store.containsKey(key), keyDoesntExistMsg);
        int intValue = getValueAsInteger(key);
        store.put(key, intValue + 1);
    }

    public void decr(String key) {
        AssertUtil.assertTrue(store.containsKey(key), keyDoesntExistMsg);
        int intValue = getValueAsInteger(key);
        store.put(key, intValue - 1);
    }

    public void incrBy(String key, int increment) {
        AssertUtil.assertTrue(store.containsKey(key), keyDoesntExistMsg);
        int intValue = getValueAsInteger(key);
        store.put(key, intValue + increment);
    }

    public void decrBy(String key, int decrement) {
        AssertUtil.assertTrue(store.containsKey(key), keyDoesntExistMsg);
        int intValue = getValueAsInteger(key);
        store.put(key, intValue - decrement);
    }

    public int lPush(String key, List<String> values) {
        ArrayList<String> list = getList(key);
        for (String value : values) list.addFirst(value);
        store.put(key, list);
        return list.size();
    }

    public int rPush(String key, List<String> values) {
        ArrayList<String> list = getList(key);
        for (String value : values) list.addLast(value);
        store.put(key, list);
        return list.size();
    }

    @SuppressWarnings("unchecked")
    public List<String> lPop(String key, int count) {
        AssertUtil.assertTrue(store.containsKey(key), keyDoesntExistMsg);
        AssertUtil.assertTrue(store.get(key) instanceof List, nonListErrorMsg);
        ArrayList<String> list = new ArrayList<>((List<String>) store.get(key));
        ArrayList<String> removeds = new ArrayList<>();
        for (int i = 0;i < count;i++) removeds.add(list.removeFirst());
        store.put(key, list);
        return removeds;
    }

    @SuppressWarnings("unchecked")
    public List<String> rPop(String key, int count) {
        AssertUtil.assertTrue(store.containsKey(key), keyDoesntExistMsg);
        AssertUtil.assertTrue(store.get(key) instanceof List, nonListErrorMsg);
        ArrayList<String> list = new ArrayList<>((List<String>) store.get(key));
        ArrayList<String> removeds = new ArrayList<>();
        for (int i = 0;i < count;i++) removeds.add(list.removeLast());
        store.put(key, list);
        return removeds;
    }

    @SuppressWarnings("unchecked")
    public List<String> lRange(String key, int start, int stop) {
        AssertUtil.assertTrue(store.containsKey(key), keyDoesntExistMsg);
        AssertUtil.assertTrue(store.get(key) instanceof List, nonListErrorMsg);
        ArrayList<String> list = new ArrayList<>((List<String>) store.get(key));
        ArrayList<String> range = new ArrayList<>();
        while (stop < 0) stop += list.size();
        for (int i = Math.max(start, 0);i <= stop && i < list.size();i++) range.add(list.get(i));
        return range;
    }

    @SuppressWarnings("unchecked")
    public String lIndex(String key, int index) {
        AssertUtil.assertTrue(store.containsKey(key), keyDoesntExistMsg);
        AssertUtil.assertTrue(store.get(key) instanceof List, nonListErrorMsg);
        ArrayList<String> list = new ArrayList<>((List<String>) store.get(key));
        while (index < 0) index += list.size();
        return index < list.size() ? list.get(index) : null;
    }

    @SuppressWarnings("unchecked")
    public boolean lSet(String key, int index, String value) {
        AssertUtil.assertTrue(store.containsKey(key), keyDoesntExistMsg);
        AssertUtil.assertTrue(store.get(key) instanceof List, nonListErrorMsg);
        ArrayList<String> list = new ArrayList<>((List<String>) store.get(key));
        while (index < 0) index += list.size();
        if (index < list.size()) list.set(index, value);
        else return false;
        store.put(key, list);
        return true;
    }

    public void setJson(String key, JSONObject value) {
        store.put(key, value);
    }

    public JSONObject getJson(String key) {
        AssertUtil.assertTrue(store.containsKey(key), keyDoesntExistMsg);
        AssertUtil.assertTrue(store.get(key) instanceof JSONObject, nonJsonErrorMsg);
        return (JSONObject) store.get(key);
    }

    public void delJson(String key) {
        AssertUtil.assertTrue(store.containsKey(key), keyDoesntExistMsg);
        AssertUtil.assertTrue(store.get(key) instanceof JSONObject, nonJsonErrorMsg);
        store.remove(key);
    }

    public int sAdd(String key, List<String> values) {
        HashSet<String> hashSet = getHashSet(key);
        int added = 0;
        for (String value : values) if (hashSet.add(value)) added++;
        store.put(key, hashSet);
        return added;
    }

    public int sRem(String key, List<String> values) {
        HashSet<String> hashSet = getHashSet(key);
        int removed = 0;
        for (String value : values) if (hashSet.remove(value)) removed++;
        store.put(key, hashSet);
        return removed;
    }

    public int sIsMember(String key, String value) {
        if (!store.containsKey(key)) return 0;
        HashSet<String> hashSet = getHashSet(key);
        return hashSet.contains(value) ? 1 : 0;
    }

    public String sMembers(String key) {
        return getHashSet(key).toString();
    }

    public List<String> sInter(List<String> keys) {
        List<HashSet<String>> hashSets = new ArrayList<>();
        for (String key : keys) hashSets.add(getHashSet(key));
        HashSet<String> intersection = new HashSet<>(hashSets.get(0));
        for (int i = 1;i < hashSets.size();i++) intersection.retainAll(hashSets.get(i));
        return new ArrayList<>(intersection);
    }

    public List<String> sUnion(List<String> keys) {
        List<HashSet<String>> hashSets = new ArrayList<>();
        for (String key : keys) hashSets.add(getHashSet(key));
        HashSet<String> union = new HashSet<>();
        for (HashSet<String> hashSet : hashSets) union.addAll(hashSet);
        return new ArrayList<>(union);
    }

    public List<String> sDiff(List<String> keys) {
        List<HashSet<String>> hashSets = new ArrayList<>();
        for (String key : keys) hashSets.add(getHashSet(key));
        HashSet<String> difference = new HashSet<>(hashSets.get(0));
        for (int i = 1;i < hashSets.size();i++) difference.removeAll(hashSets.get(i));
        return new ArrayList<>(difference);
    }

    public void jsonArrAppend(String key, Object value) {
        AssertUtil.assertTrue(store.containsKey(key), keyDoesntExistMsg);
        AssertUtil.assertTrue(store.get(key) instanceof JSONArray, nonJsonErrorMsg);
        JSONArray jsonArray = (JSONArray) store.get(key);
        jsonArray.put(value);
        store.put(key, jsonArray);
    }

    @SuppressWarnings("unchecked")
    private ArrayList<String> getList(String key) {
        ArrayList<String> list;
        if (!store.containsKey(key)) {
            list = new ArrayList<>();
        } else {
            AssertUtil.assertTrue(store.get(key) instanceof List, nonListErrorMsg);
            list = new ArrayList<>((List<String>) store.get(key));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private HashSet<String> getHashSet(String key) {
        HashSet<String> set;
        if (!store.containsKey(key)) {
            set = new HashSet<>();
        } else {
            AssertUtil.assertTrue(store.get(key) instanceof Set, "Existing value is not of type Set");
            set = new HashSet<>((Set<String>) store.get(key));
        }
        return set;
    }

    private int getValueAsInteger(String key) {
        Object value = store.get(key);
        return switch (value) {
            case Integer integer -> integer;
            case String string -> Integer.parseInt(string);
            default -> throw new AssertionError(nonIntegerErrorMsg);
        };
    }

}