package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;

public class DeleteCommand extends AbstractCommand<Object> {

    public DeleteCommand() {
        super("DELETE", 1, 1);
    }

    @Override
    protected Object processCommand(InMemoryStore store, List<String> args) {
        String key = args.get(0);
        return store.getStore().remove(key);
    }
    
}
