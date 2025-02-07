package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;

public class SetCommand extends AbstractCommand<String> {

    public SetCommand() {
        super("SET", 2, 2);
    }

    @Override
    protected String processCommand(InMemoryStore store, List<String> args) {
        store.getStore().put(args.get(0), args.get(1));
        return OK_RESPONSE;
    }
    
}
