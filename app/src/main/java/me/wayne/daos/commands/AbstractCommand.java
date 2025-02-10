package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.wayne.AssertUtil;
import me.wayne.InMemoryStore;
import me.wayne.daos.GeoMember;
import me.wayne.daos.ScoreMember;
import me.wayne.daos.StreamEntry;

public abstract class AbstractCommand<T> {
    
    protected final Logger logger = Logger.getLogger(this.getClass().getName());
    protected static final String ERROR_UNKOWN_COMMAND = "ERROR: Unkown Command";
    protected static final String INVALID_ARGS_RESPONSE = "ERROR: Invalid number of arguments";
    protected static final String OK_RESPONSE = "OK";
    protected static final String NON_INTEGER_ERROR_MSG = "Value is not of type Integer";
    protected static final String NON_STRING_ERROR_MSG = "Value is not of type String";
    protected static final String KEY_DOESNT_EXIST_MSG = "Key does not exist";
    protected static final String NON_LIST_ERROR_MSG = "Value is not of type List";
    protected static final String NON_JSON_ERROR_MSG = "Value is not of type JSONObject or JSONArray";
    protected static final String INDEX_OUT_OF_RANGE_MSG = "Index out of range";

    private final String command;

    private final int minArgs;
    private final int maxArgs;

    protected AbstractCommand(String command, int minArgs) {
        this(command, minArgs, -1);
    }

    protected AbstractCommand(String command, int minArgs, int maxArgs) {
        this.command = command;
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
    }
    
    public String getCommand() {
        return command;
    }

    public T executeCommand(Thread thread, InMemoryStore store, String inputLine) {
        List<String> args = getArgs(inputLine);
        if (args.size() < minArgs || (maxArgs != -1 && args.size() > maxArgs)) {
            throw new IllegalArgumentException(INVALID_ARGS_RESPONSE);
        }
        return processCommand(thread, store, args);
    }

    protected abstract T processCommand(Thread thread, InMemoryStore store, List<String> args);

    private List<String> getArgs(String input) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("(?:[^\\s\"]+)|\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String group1 = matcher.group(1);
            if (group1 != null) {
                group1 = group1.replace("\\\"", "\"");
                result.add(group1);
            } else {
                String group0 = matcher.group();
                group0 = group0.replace("\\\"", "\"");
                result.add(group0);
            }
        }

        result.removeFirst();

        return result;
    }

    protected int getValueAsInteger(Object obj) {
        return switch (obj) {
            case Integer integer -> integer;
            case String string -> Integer.parseInt(string);
            default -> throw new AssertionError(NON_INTEGER_ERROR_MSG);
        };
    }

    @SuppressWarnings("unchecked")
    protected ArrayList<String> getList(InMemoryStore store, String key) {
        ArrayList<String> list;
        if (!store.getStore().containsKey(key)) {
            list = new ArrayList<>();
        } else {
            AssertUtil.assertTrue(store.getStore().get(key) instanceof List, NON_LIST_ERROR_MSG);
            list = new ArrayList<>((List<String>) store.getStore().get(key));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    protected HashSet<String> getHashSet(InMemoryStore store, String key) {
        HashSet<String> set;
        if (!store.getStore().containsKey(key)) {
            set = new HashSet<>();
        } else {
            AssertUtil.assertTrue(store.getStore().get(key) instanceof Set, "Existing value is not of type Set");
            set = new HashSet<>((Set<String>) store.getStore().get(key));
        }
        return set;
    }
    
    @SuppressWarnings("unchecked")
    protected Map<String, String> getMap(InMemoryStore store, String key) {
        HashMap<String, String> map;
        if (!store.getStore().containsKey(key)) {
            map = new HashMap<>();
        } else {
            AssertUtil.assertTrue(store.getStore().get(key) instanceof Map, "Existing value is not of type Map");
            map = new HashMap<>((Map<String, String>) store.getStore().get(key));
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    protected TreeSet<ScoreMember> getTreeSet(InMemoryStore store, String key) {
        TreeSet<ScoreMember> set = new TreeSet<>();
        if (store.getStore().containsKey(key)) {
            AssertUtil.assertTrue(store.getStore().get(key) instanceof SortedSet, "Existing value is not of type SortedSet");
            SortedSet<?> unknownSet = (SortedSet<?>) store.getStore().get(key);
            if (!unknownSet.isEmpty()) {
                AssertUtil.assertTrue(unknownSet.first() instanceof ScoreMember, "Existing value elements is not of type ScoreMember");
            }
            set.addAll((SortedSet<ScoreMember>) store.getStore().get(key));
        }
        return set;
    }

    @SuppressWarnings("unchecked")
    protected TreeSet<GeoMember> getGeoSet(InMemoryStore store, String key) {
        TreeSet<GeoMember> set = new TreeSet<>();
        if (store.getStore().containsKey(key)) {
            AssertUtil.assertTrue(store.getStore().get(key) instanceof SortedSet, "Existing value is not of type SortedSet");
            SortedSet<?> unknownSet = (SortedSet<?>) store.getStore().get(key);
            if (!unknownSet.isEmpty()) {
                AssertUtil.assertTrue(unknownSet.first() instanceof GeoMember, "Existing value elements is not of type GeoMember");
            }
            set.addAll((SortedSet<GeoMember>) store.getStore().get(key));
        }
        return set;
    }

    protected ScoreMember getScoreMember(InMemoryStore store, String key, String member) {
        Map<String, Integer> members = store.getTreeSetMembers().get(key);
        if (members == null) return null;
        Integer score = members.get(member);
        return score == null ? null : new ScoreMember(score, member);
    }

    protected void addToTreeSetMembers(InMemoryStore store, String key, ScoreMember scoreMember) {
        store.getTreeSetMembers().computeIfAbsent(key, k -> new HashMap<>())   .put(scoreMember.getMember(), scoreMember.getScore());
    }

    protected void removeFromTreeSetMembers(InMemoryStore store, String key, ScoreMember scoreMember) {
        Map<String, Integer> members = store.getTreeSetMembers().get(key);
        if (members != null) members.remove(scoreMember.getMember());
    }

    @SuppressWarnings("unchecked")
    protected ArrayList<StreamEntry> getStreamList(InMemoryStore store, String key) {
        ArrayList<StreamEntry> list;
        if (!store.getStore().containsKey(key)) {
            list = new ArrayList<>();
        } else {
            AssertUtil.assertTrue(store.getStore().get(key) instanceof List, NON_LIST_ERROR_MSG);
            list = new ArrayList<>((ArrayList<StreamEntry>) store.getStore().get(key));
        }
        return list;
    }
    
}
