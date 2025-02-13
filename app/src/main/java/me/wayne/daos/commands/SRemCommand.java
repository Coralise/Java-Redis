package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.StoreSet;

public class SRemCommand extends AbstractCommand<Integer> {

    public SRemCommand() {
        super("SREM", 2);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        List<String> values = args.subList(1, args.size());
        StoreSet hashSet = store.getStoreValue(key, StoreSet.class);
        if (hashSet == null) return 0;
        int removed = 0;
        for (String value : values) if (hashSet.remove(value)) removed++;
        store.setStoreValue(key, hashSet, inputLine);
        return removed;
    }
    
}
