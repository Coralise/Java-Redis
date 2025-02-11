package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.StoreSet;

public class SMembersCommand extends AbstractCommand<String> {

    public SMembersCommand() {
        super("SMEMBERS", 1, 1);
    }

    @Override
    protected String processCommand(PrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        StoreSet storeValue = store.getStoreValue(key, StoreSet.class);
        if (storeValue == null) return null;
        return storeValue.toString();
    }
    
}
