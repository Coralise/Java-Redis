package me.wayne.daos.commands;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;

public class GetCommand extends AbstractCommand<Object> {

    public GetCommand() {
        super("GET", 1, 1);
    }

    @Override
    protected Object processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        return store.getStoreValue(key, Object.class);
    }

}