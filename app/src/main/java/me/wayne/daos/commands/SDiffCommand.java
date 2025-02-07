package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.wayne.InMemoryStore;

public class SDiffCommand extends AbstractCommand<List<String>> {

    public SDiffCommand() {
        super("SDIFF", 1);
    }

    @Override
    protected List<String> processCommand(Thread thread, InMemoryStore store, List<String> args) {
        List<String> keys = args;
        List<HashSet<String>> hashSets = new ArrayList<>();
        for (String key : keys) hashSets.add(getHashSet(store, key));
        HashSet<String> difference = new HashSet<>(hashSets.get(0));
        for (int i = 1;i < hashSets.size();i++) difference.removeAll(hashSets.get(i));
        return new ArrayList<>(difference);
    }
    
}
