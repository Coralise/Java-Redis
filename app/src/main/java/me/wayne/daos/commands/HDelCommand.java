package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.StoreMap;

public class HDelCommand extends AbstractCommand<Integer> {

    public HDelCommand() {
        super("HDEL", 2);
    }

    @Override
    protected Integer processCommand(PrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        List<String> fields = args.subList(1, args.size());
        StoreMap hashMap = store.getStoreValue(key, StoreMap.class);
        if (hashMap == null) return 0;
        int removed = 0;
        for (String field : fields) if (hashMap.remove(field) != null) removed++;
        store.setStoreValue(key, hashMap);
        return removed;
    }
    
}
