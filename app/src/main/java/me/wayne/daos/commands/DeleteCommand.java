package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.StoreValue;

public class DeleteCommand extends AbstractCommand<Object> {

    public DeleteCommand() {
        super("DELETE", 1, 1);
    }

    @Override
    protected Object processCommand(PrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        StoreValue storeValue = store.removeStoreValue(key);
        return storeValue != null ? storeValue.getValue() : null;
    }
    
}
