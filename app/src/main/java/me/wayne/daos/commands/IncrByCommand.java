package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.io.StorePrintWriter;

public class IncrByCommand extends AbstractCommand<String> {

    public IncrByCommand() {
        super("INCRBY", 2, 2);
    }

    @Override
    protected String processCommand(StorePrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        int increment = getValueAsInteger(args.get(1));
        int intValue = getValueAsInteger(store.getStoreValue(key, true).getValue());
        store.setStoreValue(key, intValue + increment);
        return OK_RESPONSE;
    }
    
}
