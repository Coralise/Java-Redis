package me.wayne.daos.commands;

import java.util.List;

import me.wayne.daos.StoreSet;
import me.wayne.daos.io.StorePrintWriter;

public class SMembersCommand extends AbstractCommand<String> {

    public SMembersCommand() {
        super("SMEMBERS", 1, 1);
    }

    @Override
    protected String processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        StoreSet storeValue = store.getStoreValue(key, StoreSet.class);
        if (storeValue == null) return null;
        return storeValue.toString();
    }
    
}
