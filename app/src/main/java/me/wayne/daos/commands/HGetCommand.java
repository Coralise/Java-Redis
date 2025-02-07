package me.wayne.daos.commands;

import java.util.List;
import java.util.Map;

import me.wayne.InMemoryStore;

public class HGetCommand extends AbstractCommand<String> {

    public HGetCommand() {
        super("HGET", 2, 2);
    }

    @Override
    protected String processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        String field = args.get(1);
        if (!store.getStore().containsKey(key)) return null;
        Map<String, String> hashMap = getMap(store, key);
        return hashMap.get(field);
    }
    
}
