package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.StoreList;
import me.wayne.daos.io.StorePrintWriter;

public class RPushCommand extends AbstractCommand<Integer> {

    public RPushCommand() {
        super("RPUSH", 2);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        List<String> values = args.subList(1, args.size());
        StoreList list = store.getStoreValue(key, StoreList.class, new StoreList());
        for (String value : values) list.addLast(value);
        store.setStoreValue(key, list);
        return list.size();
    }
    
}