package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;

public class GetCommand extends AbstractCommand<Object> {

    public GetCommand() {
        super("GET", 1, 1);
    }

    @Override
    protected Object processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        return store.getStore().get(key);
    }

}