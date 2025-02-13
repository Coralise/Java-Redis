package me.wayne.daos.commands;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.StoreSet;

public class SIsMemberCommand extends AbstractCommand<Integer> {

    public SIsMemberCommand() {
        super("SISMEMBER", 2, 2);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        String value = args.get(1);
        StoreSet hashSet = store.getStoreValue(key, StoreSet.class);
        if (hashSet == null) return 0;
        return hashSet.contains(value) ? 1 : 0;
    }
    
}
