package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.wayne.daos.StoreSet;
import me.wayne.daos.StoreValue;
import me.wayne.daos.io.StorePrintWriter;

public class SUnionCommand extends AbstractCommand<Object> {

    public SUnionCommand() {
        super("SUNION", 1);
    }

    @Override
    protected Object processCommand(StorePrintWriter out, List<String> args) {
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
