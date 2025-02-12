package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.StoreList;
import me.wayne.daos.StoreValue;
import me.wayne.daos.io.StorePrintWriter;

public class RPopCommand extends AbstractCommand<List<String>> {

    public RPopCommand() {
        super("RPOP", 1, 2);
    }

    @Override
    protected List<String> processCommand(StorePrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        int count = args.size() > 1 ? Integer.parseInt(args.get(1)) : 1;
        StoreValue storeValue = store.getStoreValue(key, true);
        StoreList list = storeValue.getValue(StoreList.class);
        ArrayList<String> removeds = new ArrayList<>();
        for (int i = 0;i < count;i++) removeds.add(list.removeLast());
        store.setStoreValue(key, list);
        return removeds;
    }
    
}
