package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.StoreSet;
import me.wayne.daos.StoreValue;
import me.wayne.daos.io.StorePrintWriter;

public class SAddCommand extends AbstractCommand<Integer> {

    public SAddCommand() {
        super("SADD", 2);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        List<String> values = args.subList(1, args.size());
        StoreValue storeValue = store.getStoreValue(key);
        StoreSet hashSet = storeValue == null ? new StoreSet() : storeValue.getValue(StoreSet.class);
        int added = 0;
        for (String value : values) if (hashSet.add(value)) added++;
        store.setStoreValue(key, hashSet);
        return added;
    }
    
}
