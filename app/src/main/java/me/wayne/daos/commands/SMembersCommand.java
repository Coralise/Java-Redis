package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;

public class SMembersCommand extends AbstractCommand<String> {

    public SMembersCommand() {
        super("SMEMBERS", 1, 1);
    }

    @Override
    protected String processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        return getHashSet(store, key).toString();
    }
    
}
