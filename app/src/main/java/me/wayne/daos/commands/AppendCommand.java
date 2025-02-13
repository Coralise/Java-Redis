package me.wayne.daos.commands;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.StoreValue;

public class AppendCommand extends AbstractCommand<String> {

    public AppendCommand() {
        super("APPEND", 2, 2);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        String value = args.get(1);
        StoreValue storeValue = store.getStoreValue(key, true);
        String newValue = storeValue.getValue(String.class) + value;
        store.setStoreValue(key, newValue, inputLine);
        return OK_RESPONSE;
    }
    
}
