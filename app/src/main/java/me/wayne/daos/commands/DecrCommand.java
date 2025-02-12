package me.wayne.daos.commands;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;

public class DecrCommand extends AbstractCommand<String> {

    public DecrCommand() {
        super("DECR", 1, 1);
    }

    @Override
    protected String processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        int intValue = getValueAsInteger(store.getStoreValue(key, Object.class));
        store.setStoreValue(key, intValue - 1);
        return OK_RESPONSE;
    }
    
}
