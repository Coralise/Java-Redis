package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.StoreSet;

public class SRemCommand extends AbstractCommand<Integer> {

    public SRemCommand() {
        super("SREM", 2);
    }

    @Override
    protected Integer processCommand(PrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        List<String> values = args.subList(1, args.size());
        StoreSet hashSet = store.getStoreValue(key, StoreSet.class);
        if (hashSet == null) return 0;
        int removed = 0;
        for (String value : values) if (hashSet.remove(value)) removed++;
        store.setStoreValue(key, hashSet);
        return removed;
    }
    
}
