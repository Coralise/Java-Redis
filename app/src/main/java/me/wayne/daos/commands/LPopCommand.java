package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.StoreList;
import me.wayne.daos.StoreValue;

public class LPopCommand extends AbstractCommand<List<String>> {

    public LPopCommand() {
        super("LPOP", 1, 2);
    }

    @Override
    protected List<String> processCommand(PrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        int count = args.size() > 1 ? Integer.parseInt(args.get(1)) : 1;
        StoreValue storeValue = store.getStoreValue(key, true);
        StoreList list = storeValue.getValue(StoreList.class);
        ArrayList<String> removeds = new ArrayList<>();
        for (int i = 0;i < count;i++) removeds.add(list.removeFirst());
        store.setStoreValue(key, list);
        return removeds;
    }
    
}
