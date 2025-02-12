package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.io.StorePrintWriter;

public class ExistsCommand extends AbstractCommand<Integer> {

    public ExistsCommand() {
        super("EXISTS", 1);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, InMemoryStore store, List<String> args) {
        int count = 0;
        for (String key : args) if (store.hasStoreValue(key)) count++;
        return count;
    }
    
}
