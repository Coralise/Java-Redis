package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.wayne.InMemoryStore;

public class SUnionCommand extends AbstractCommand<Object> {

    public SUnionCommand() {
        super("SUNION", 1);
    }

    @Override
    protected Object processCommand(InMemoryStore store, List<String> args) {
        List<String> keys = args;
        List<HashSet<String>> hashSets = new ArrayList<>();
        for (String key : keys) hashSets.add(getHashSet(store, key));
        HashSet<String> union = new HashSet<>();
        for (HashSet<String> hashSet : hashSets) union.addAll(hashSet);
        return new ArrayList<>(union);
    }
    
}
