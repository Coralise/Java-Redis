package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.List;

import me.wayne.InMemoryStore;

public class IncrCommand extends AbstractCommand<String> {

    public IncrCommand() {
        super("INCR", 1, 1);
    }

    @Override
    protected String processCommand(PrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        int intValue = getValueAsInteger(store.getStoreValue(key, true).getValue());
        store.setStoreValue(key, intValue + 1);
        return OK_RESPONSE;
    }
    
}
