package me.wayne.daos.commands;

import java.util.HashSet;
import java.util.List;

import me.wayne.InMemoryStore;

public class SRemCommand extends AbstractCommand<Integer> {

    public SRemCommand() {
        super("SREM", 2);
    }

    @Override
    protected Integer processCommand(InMemoryStore store, List<String> args) {
        String key = args.get(0);
        List<String> values = args.subList(1, args.size());
        HashSet<String> hashSet = getHashSet(store, key);
        int removed = 0;
        for (String value : values) if (hashSet.remove(value)) removed++;
        store.getStore().put(key, hashSet);
        return removed;
    }
    
}
