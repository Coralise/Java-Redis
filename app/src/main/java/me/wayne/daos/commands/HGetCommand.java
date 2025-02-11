package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.StoreMap;

public class HGetCommand extends AbstractCommand<String> {

    public HGetCommand() {
        super("HGET", 2, 2);
    }

    @Override
    protected String processCommand(PrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        String field = args.get(1);
        StoreMap hashMap = store.getStoreValue(key, StoreMap.class);
        if (hashMap == null) return null;
        return hashMap.get(field);
    }
    
}
