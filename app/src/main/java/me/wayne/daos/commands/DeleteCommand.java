package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.StoreValue;

public class DeleteCommand extends AbstractCommand<Object> {

    public DeleteCommand() {
        super("DELETE", 1, 1);
    }

    @Override
    protected Object processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        StoreValue storeValue = store.removeStoreValue(key, inputLine);
        return storeValue != null ? storeValue.getValue() : null;
    }
    
}
