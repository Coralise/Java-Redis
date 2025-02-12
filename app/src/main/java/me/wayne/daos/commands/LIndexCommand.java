package me.wayne.daos.commands;

import java.util.List;

import me.wayne.daos.StoreList;
import me.wayne.daos.StoreValue;
import me.wayne.daos.io.StorePrintWriter;

public class LIndexCommand extends AbstractCommand<String> {

    public LIndexCommand() {
        super("LINDEX", 2, 2);
    }

    @Override
    protected String processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        int index = Integer.parseInt(args.get(1));
        StoreValue storeValue = store.getStoreValue(key, true);
        StoreList list = storeValue.getValue(StoreList.class);
        while (index < 0) index += list.size();
        return index < list.size() ? list.get(index) : null;
    }
    
}
