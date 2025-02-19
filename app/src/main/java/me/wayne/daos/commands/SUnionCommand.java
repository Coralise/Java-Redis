package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.PrintableList;
import me.wayne.daos.storevalues.StoreSet;
import me.wayne.daos.storevalues.StoreValue;

public class SUnionCommand extends AbstractCommand<Object> {

    public SUnionCommand() {
        super("SUNION", 1);
    }

    @Override
    protected Object processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        List<String> keys = args;
        List<StoreSet> hashSets = new ArrayList<>();
        for (String key : keys) {
            StoreValue storeValue = store.getStoreValue(key);
            StoreSet hashSet = storeValue == null ? new StoreSet() : storeValue.getValue(StoreSet.class);
            hashSets.add(hashSet);
        }
        Set<String> union = StoreSet.union(hashSets);
        return new PrintableList<>(union);
    }
    
}
