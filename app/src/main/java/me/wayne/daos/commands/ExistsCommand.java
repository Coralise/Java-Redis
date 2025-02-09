package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;

public class ExistsCommand extends AbstractCommand<Integer> {

    public ExistsCommand() {
        super("EXISTS", 1);
    }

    @Override
    protected Integer processCommand(Thread thread, InMemoryStore store, List<String> args) {
        int count = 0;
        for (String key : args) if (store.getStore().containsKey(key)) count++;
        return count;
    }
    
}
