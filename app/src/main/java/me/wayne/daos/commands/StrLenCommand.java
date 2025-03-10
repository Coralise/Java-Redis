package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;

public class StrLenCommand extends AbstractCommand<Integer> {

    public StrLenCommand() {
        super("STRLEN", 1, 1);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        return store.getStoreValue(key, true).getValue(String.class).length();
    }
    
}
