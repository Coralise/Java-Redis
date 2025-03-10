package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;

public class IncrCommand extends AbstractCommand<String> {

    public IncrCommand() {
        super("INCR", 1, 1);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        int intValue = getValueAsInteger(store.getStoreValue(key, true).getValue());
        store.setStoreValue(key, intValue + 1, inputLine);
        return OK_RESPONSE;
    }
    
}
