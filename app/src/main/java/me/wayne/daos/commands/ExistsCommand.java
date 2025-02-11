package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.List;

import me.wayne.InMemoryStore;

public class ExistsCommand extends AbstractCommand<Integer> {

    public ExistsCommand() {
        super("EXISTS", 1);
    }

    @Override
    protected Integer processCommand(PrintWriter out, InMemoryStore store, List<String> args) {
        int count = 0;
        for (String key : args) if (store.hasStoreValue(key)) count++;
        return count;
    }
    
}
