package me.wayne.daos.commands;

import java.util.List;

import me.wayne.daos.StoreValue;
import me.wayne.daos.io.StorePrintWriter;

public class TtlCommand extends AbstractCommand<Integer> {

    public TtlCommand() {
        super("TTL", 1);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        StoreValue storeValue = store.getStoreValue(key, false);
        if (storeValue == null) return -2;
        return storeValue.getTimeToLive();
    }
    
}