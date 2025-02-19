package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.PrintableList;
import me.wayne.daos.storevalues.StoreList;
import me.wayne.daos.storevalues.StoreValue;

public class LRangeCommand extends AbstractCommand<List<String>> {

    public LRangeCommand() {
        super("LRANGE", 3, 3);
    }

    @Override
    protected List<String> processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        int start = Integer.parseInt(args.get(1));
        int stop = Integer.parseInt(args.get(2));
        StoreValue storeValue = store.getStoreValue(key, true);
        StoreList list = storeValue.getValue(StoreList.class);
        return list.range(start, stop);
    }
    
}
