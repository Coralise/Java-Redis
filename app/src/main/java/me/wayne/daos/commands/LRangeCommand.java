package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.List;

import me.wayne.daos.StoreList;
import me.wayne.daos.StoreValue;
import me.wayne.daos.io.StorePrintWriter;

public class LRangeCommand extends AbstractCommand<List<String>> {

    public LRangeCommand() {
        super("LRANGE", 3, 3);
    }

    @Override
    protected List<String> processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        int start = Integer.parseInt(args.get(1));
        int stop = Integer.parseInt(args.get(2));
        StoreValue storeValue = store.getStoreValue(key, true);
        StoreList list = storeValue.getValue(StoreList.class);
        ArrayList<String> range = new ArrayList<>();
        while (stop < 0) stop += list.size();
        for (int i = Math.max(start, 0);i <= stop && i < list.size();i++) range.add(list.get(i));
        return range;
    }
    
}
