package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.StoreList;

public class RPushCommand extends AbstractCommand<Integer> {

    public RPushCommand() {
        super("RPUSH", 2);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        List<String> values = args.subList(1, args.size());
        StoreList list = store.getStoreValue(key, StoreList.class, new StoreList());
        list.rPush(values);
        store.setStoreValue(key, list, inputLine);
        return list.size();
    }
    
}