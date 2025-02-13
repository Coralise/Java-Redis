package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;

public class GetRangeCommand extends AbstractCommand<String> {

    public GetRangeCommand() {
        super("GETRANGE", 3, 3);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        int start = Integer.parseInt(args.get(1));
        int end = Integer.parseInt(args.get(2));
        
        String value = store.getStoreValue(key, true).getValue(String.class);
        return value.substring(start, end + 1);
    }
    
}
