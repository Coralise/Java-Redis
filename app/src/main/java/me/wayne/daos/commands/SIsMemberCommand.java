package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.StoreSet;

public class SIsMemberCommand extends AbstractCommand<Integer> {

    public SIsMemberCommand() {
        super("SISMEMBER", 2, 2);
    }

    @Override
    protected Integer processCommand(PrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        String value = args.get(1);
        StoreSet hashSet = store.getStoreValue(key, StoreSet.class);
        if (hashSet == null) return 0;
        return hashSet.contains(value) ? 1 : 0;
    }
    
}
