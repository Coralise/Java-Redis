package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.List;

import me.wayne.InMemoryStore;

public class RPushCommand extends AbstractCommand<Integer> {

    public RPushCommand() {
        super("RPUSH", 2);
    }

    @Override
    protected Integer processCommand(InMemoryStore store, List<String> args) {
        String key = args.get(0);
        List<String> values = args.subList(1, args.size());
        ArrayList<String> list = getList(store, key);
        for (String value : values) list.addLast(value);
        store.getStore().put(key, list);
        return list.size();
    }
    
}