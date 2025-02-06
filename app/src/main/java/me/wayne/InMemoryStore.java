package me.wayne;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;

class InMemoryStore {

    private final Map<String, Object> store = new HashMap<>();

    private final Map<String, Map<String, Integer>> treeSetMembers = new HashMap<>();
    
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

    public int hSet(String key, List<String> fieldsAndValues) {
        HashMap<String, String> hashMap = new HashMap<>();
        int added = 0;
        for (int i = 1; i < fieldsAndValues.size(); i += 2) {
            hashMap.put(fieldsAndValues.get(i-1), fieldsAndValues.get(i));
            added++;
        }
        store.put(key, hashMap);
        return added;
    }

    public String hGet(String key, String field) {
        if (!store.containsKey(key)) return null;
        HashMap<String, String> hashMap = getHashMap(key);
        return hashMap.get(field);
    }

    public List<String> hGetAll(String key) {
        if (!store.containsKey(key)) return new ArrayList<>();
        HashMap<String, String> hashMap = getHashMap(key);
        List<String> fieldsAndValues = new ArrayList<>();
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            fieldsAndValues.add(entry.getKey());
            fieldsAndValues.add(entry.getValue());
        }
        return fieldsAndValues;
    }

    public int hDel(String key, List<String> fields) {
        if (!store.containsKey(key)) return 0;
        HashMap<String, String> hashMap = getHashMap(key);
        int removed = 0;
        for (String field : fields) if (hashMap.remove(field) != null) removed++;
        store.put(key, hashMap);
        return removed;
    }

    public int hExists(String key, String field) {
        if (!store.containsKey(key)) return 0;
        HashMap<String, String> hashMap = getHashMap(key);
        return hashMap.containsKey(field) ? 1 : 0;
    }

    public int zAdd(String key, List<String> options, List<String> scoresAndMembers) {
        TreeSet<ScoreMember> treeSet = getTreeSet(key);

        boolean nx = options.contains("NX");
        boolean xx = options.contains("XX");
        AssertUtil.assertTrue(!(nx && xx), "Both NX and XX options are specified");
        boolean gt = options.contains("GT");
        boolean lt = options.contains("LT");
        AssertUtil.assertTrue(!(gt && lt), "Both GT and LT options are specified");
        boolean ch = options.contains("CH");
        boolean incr = options.contains("INCR");

        if (incr) AssertUtil.assertTrue(scoresAndMembers.size() == 2, "INCR option requires a single score and member");

        int updated = 0;

        for (int i = 1; i < scoresAndMembers.size(); i += 2) {
            int score = parseScore(scoresAndMembers.get(i-1));
            ScoreMember scoreMember = new ScoreMember(score, scoresAndMembers.get(i));
            updated += processScoreMember(key, treeSet, nx, xx, gt, lt, ch, incr, scoreMember);
        }

        store.put(key, treeSet);

        return updated;
    }

    private int parseScore(String scoreStr) {
        try {
            return Integer.parseInt(scoreStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid score: " + scoreStr, e);
        }
    }

    private int processScoreMember(String key, TreeSet<ScoreMember> treeSet, boolean nx, boolean xx, boolean gt, boolean lt, boolean ch, boolean incr, ScoreMember scoreMember) {
        ScoreMember existingScoreMember = getScoreMember(key, scoreMember.getMember());
        if (existingScoreMember != null) {
            if (nx) return 0;
            if (gt && scoreMember.getScore() <= existingScoreMember.getScore()) return 0;
            if (lt && scoreMember.getScore() >= existingScoreMember.getScore()) return 0;

            if (incr) scoreMember = new ScoreMember(scoreMember.getScore() + existingScoreMember.getScore(), scoreMember.getMember());
            else if (scoreMember.getScore().equals(existingScoreMember.getScore())) return 0;

            treeSet.remove(existingScoreMember);
            treeSet.add(scoreMember);
            addToTreeSetMembers(key, scoreMember);
            return ch ? 1 : 0;
        } else {
            if (xx) return 0;
            treeSet.add(scoreMember);
            addToTreeSetMembers(key, scoreMember);
            return 1;
        }
    }

    public List<String> zRange(String key, int start, int stop, List<String> options) {
        TreeSet<ScoreMember> treeSet = getTreeSet(key);
        List<String> range = new ArrayList<>();
        int i = 0;
        while (start < 0) start += treeSet.size();
        while (stop < 0) stop += treeSet.size();

        boolean withScores = options.contains("WITHSCORES");
        boolean rev = options.contains("REV");

        for (ScoreMember scoreMember : treeSet) {
            if (i >= start && i <= stop) {
                range.add(scoreMember.getMember());
                if (withScores) range.add(scoreMember.getScore().toString());
            }
            i++;
        }
        return !rev ? range : range.reversed();
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
    private HashMap<String, String> getHashMap(String key) {
        HashMap<String, String> map;
        if (!store.containsKey(key)) {
            map = new HashMap<>();
        } else {
            AssertUtil.assertTrue(store.get(key) instanceof Map, "Existing value is not of type Map");
            map = new HashMap<>((Map<String, String>) store.get(key));
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private TreeSet<ScoreMember> getTreeSet(String key) {
        Comparator<ScoreMember> integerComparator = (s1, s2) -> s1.getScore().compareTo(s2.getScore());
        TreeSet<ScoreMember> set = new TreeSet<>(integerComparator);
        if (store.containsKey(key)) {
            AssertUtil.assertTrue(store.get(key) instanceof SortedSet, "Existing value is not of type SortedSet");
            set.addAll((SortedSet<ScoreMember>) store.get(key));
        }
        return set;
    }

    private ScoreMember getScoreMember(String key, String member) {
        Map<String, Integer> members = treeSetMembers.get(key);
        if (members == null) return null;
        Integer score = members.get(member);
        return score == null ? null : new ScoreMember(score, member);
    }

    private void addToTreeSetMembers(String key, ScoreMember scoreMember) {
        treeSetMembers.computeIfAbsent(key, k -> new HashMap<>())   .put(scoreMember.getMember(), scoreMember.getScore());
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