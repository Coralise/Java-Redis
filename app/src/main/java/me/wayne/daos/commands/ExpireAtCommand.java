package me.wayne.daos.commands;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.StoreValue;

public class ExpireAtCommand extends AbstractCommand<Integer> {

    public ExpireAtCommand() {
        super("EXPIREAT", 2, 3);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        long timestamp = Long.parseLong(args.get(1));
        boolean nx = false;
        boolean  xx = false;
        boolean  lt = false;
        boolean  gt = false;
        if (args.size() == 3) {
            String option = args.get(2).toUpperCase();
            nx = option.equals("NX");
            xx = option.equals("XX");
            lt = option.equals("LT");
            gt = option.equals("GT");
        }

        StoreValue storeValue = store.getStoreValue(key);
        if (storeValue == null) {
            return 0;
        }
        
        return storeValue.setExpiryAtDate(timestamp, nx, xx, gt, lt, key);
    }
    
}
