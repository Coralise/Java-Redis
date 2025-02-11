package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.StoreSet;
import me.wayne.daos.StoreValue;

public class SUnionCommand extends AbstractCommand<Object> {

    public SUnionCommand() {
        super("SUNION", 1);
    }

    @Override
    protected Object processCommand(PrintWriter out, InMemoryStore store, List<String> args) {
        List<String> keys = args;
        List<StoreSet> hashSets = new ArrayList<>();
        for (String key : keys) {
            StoreValue storeValue = store.getStoreValue(key);
            StoreSet hashSet = storeValue == null ? new StoreSet() : storeValue.getValue(StoreSet.class);
            hashSets.add(hashSet);
        }
        HashSet<String> union = new HashSet<>();
        for (StoreSet hashSet : hashSets) union.addAll(hashSet);
        return new ArrayList<>(union);
    }
    
}
