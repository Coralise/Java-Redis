package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.wayne.InMemoryStore;

public class SInterCommand extends AbstractCommand<List<String>> {

    public SInterCommand() {
        super("SINTER", 1);
    }

    @Override
    protected List<String> processCommand(Thread thread, InMemoryStore store, List<String> args) {
        List<String> keys = args;
        List<HashSet<String>> hashSets = new ArrayList<>();
        for (String key : keys) hashSets.add(getHashSet(store, key));
        HashSet<String> intersection = new HashSet<>(hashSets.get(0));
        for (int i = 1;i < hashSets.size();i++) intersection.retainAll(hashSets.get(i));
        return new ArrayList<>(intersection);
    }
    
}
