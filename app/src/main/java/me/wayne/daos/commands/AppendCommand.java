package me.wayne.daos.commands;

import java.util.List;

import me.wayne.daos.StoreValue;
import me.wayne.daos.io.StorePrintWriter;

public class AppendCommand extends AbstractCommand<String> {

    public AppendCommand() {
        super("APPEND", 2, 2);
    }

    @Override
    protected String processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        String value = args.get(1);
        StoreValue storeValue = store.getStoreValue(key, true);
        String newValue = storeValue.getValue(String.class) + value;
        store.setStoreValue(key, newValue);
        return OK_RESPONSE;
    }
    
}
