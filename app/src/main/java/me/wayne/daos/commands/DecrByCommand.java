package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;

public class DecrByCommand extends AbstractCommand<String> {

    public DecrByCommand() {
        super("DECRBY", 2, 2);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        int decrement = getValueAsInteger(args.get(1));
        int intValue = getValueAsInteger(store.getStoreValue(key, Object.class));
        store.setStoreValue(key, intValue - decrement, inputLine);
        return OK_RESPONSE;
    }
    
}
