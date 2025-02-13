package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.StoreSet;

public class SMembersCommand extends AbstractCommand<String> {

    public SMembersCommand() {
        super("SMEMBERS", 1, 1);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        StoreSet storeValue = store.getStoreValue(key, StoreSet.class);
        if (storeValue == null) return "[]";
        return storeValue.toString();
    }
    
}
