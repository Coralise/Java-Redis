package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.StoreList;
import me.wayne.daos.storevalues.StoreValue;

public class LPopCommand extends AbstractCommand<List<String>> {

    public LPopCommand() {
        super("LPOP", 1, 2);
    }

    @Override
    protected List<String> processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        int count = args.size() > 1 ? Integer.parseInt(args.get(1)) : 1;
        StoreValue storeValue = store.getStoreValue(key, true);
        StoreList list = storeValue.getValue(StoreList.class);
        List<String> removeds = list.lPop(count);
        store.setStoreValue(key, list, inputLine);
        return removeds;
    }
    
}
