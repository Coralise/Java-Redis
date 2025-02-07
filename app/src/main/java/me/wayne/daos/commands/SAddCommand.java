package me.wayne.daos.commands;

import java.util.HashSet;
import java.util.List;

import me.wayne.InMemoryStore;

public class SAddCommand extends AbstractCommand<Integer> {

    public SAddCommand() {
        super("SADD", 2);
    }

    @Override
    protected Integer processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        List<String> values = args.subList(1, args.size());
        HashSet<String> hashSet = getHashSet(store, key);
        int added = 0;
        for (String value : values) if (hashSet.add(value)) added++;
        store.getStore().put(key, hashSet);
        return added;
    }
    
}
