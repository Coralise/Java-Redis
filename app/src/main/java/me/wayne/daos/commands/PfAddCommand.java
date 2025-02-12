package me.wayne.daos.commands;

import java.util.List;

import me.wayne.daos.StoreValue;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.probabilistic.HyperLogLog;

public class PfAddCommand extends AbstractCommand<Integer> {

    public PfAddCommand() {
        super("PFADD", 2);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        List<String> values = args.subList(1, args.size());

        StoreValue storeValue = store.getStoreValue(key);
        HyperLogLog hyperLogLog = storeValue == null ? new HyperLogLog(15) : storeValue.getValue(HyperLogLog.class);

        boolean updated = false;
        double oldEstimate = hyperLogLog.estimate();
        for (String value : values) {
            hyperLogLog.add(value);
            if (!updated) updated = oldEstimate != hyperLogLog.estimate();
        }

        store.setStoreValue(key, hyperLogLog);
        return updated ? 1 : 0;
    }
    
}
