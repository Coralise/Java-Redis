package me.wayne.daos.commands;

import java.util.List;

import me.wayne.daos.StoreValue;
import me.wayne.daos.io.StorePrintWriter;

public class DeleteCommand extends AbstractCommand<Object> {

    public DeleteCommand() {
        super("DELETE", 1, 1);
    }

    @Override
    protected Object processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        StoreValue storeValue = store.removeStoreValue(key);
        return storeValue != null ? storeValue.getValue() : null;
    }
    
}
