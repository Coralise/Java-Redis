package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.List;

import me.wayne.InMemoryStore;

public class StrLenCommand extends AbstractCommand<Integer> {

    public StrLenCommand() {
        super("STRLEN", 1, 1);
    }

    @Override
    protected Integer processCommand(PrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        return store.getStoreValue(key, true).getValue(String.class).length();
    }
    
}
