package me.wayne.daos.commands;

import java.util.List;

import me.wayne.daos.StoreList;
import me.wayne.daos.StoreValue;
import me.wayne.daos.io.StorePrintWriter;

public class LSetCommand extends AbstractCommand<String> {

    public LSetCommand() {
        super("LSET", 3, 3);
    }

    @Override
    protected String processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        int index = Integer.parseInt(args.get(1));
        String value = args.get(2);
        StoreValue storeValue = store.getStoreValue(key, true);
        StoreList list = storeValue.getValue(StoreList.class);
        while (index < 0) index += list.size();
        if (index >= list.size()) return INDEX_OUT_OF_RANGE_MSG;
        list.set(index, value); 
        store.setStoreValue(key, list);
        return OK_RESPONSE;
    }
    
}
