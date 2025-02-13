package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.StoreValue;

public class ExpireCommand extends AbstractCommand<Integer> {

    public ExpireCommand() {
        super("EXPIRE", 2, 3);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        long seconds = Integer.parseInt(args.get(1));
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

        String aofCommand = "EXPIREAT " + key + " " + (System.currentTimeMillis() + seconds * 1000);
        if (args.size() == 3) {
            aofCommand += " " + args.get(2);
        }
        
        return store.setExpiryInMillis(storeValue, seconds * 1000, nx, xx, gt, lt, key, aofCommand);
    }
    
}
