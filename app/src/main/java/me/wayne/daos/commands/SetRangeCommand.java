package me.wayne.daos.commands;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.StoreValue;

public class SetRangeCommand extends AbstractCommand<String> {

    public SetRangeCommand() {
        super("SETRANGE", 3, 3);
    }

    @Override
    protected String processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        int offset = Integer.parseInt(args.get(1));
        String value = args.get(2);
        StoreValue storeValue = store.getStoreValue(key, true);
        String existingValue = storeValue.getValue(String.class);
        StringBuilder newValue = new StringBuilder(existingValue);
        newValue.replace(offset, offset + value.length(), value);
        store.setStoreValue(key, newValue.toString());
        return newValue.toString();
    }
    
}
