package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.StoreValue;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.probabilistic.HyperLogLog;

public class PfMergeCommand extends AbstractCommand<String> {

    public PfMergeCommand() {
        super("PFMERGE", 2);
    }

    @Override
    protected String processCommand(StorePrintWriter out, InMemoryStore store, List<String> args) {
        
        String destKey = args.get(0);
        List<HyperLogLog> hyperLogLogs = args.subList(1, args.size()).stream().map(key -> {
            StoreValue storeValue = store.getStoreValue(key);
            return storeValue == null ? new HyperLogLog(15) : storeValue.getValue(HyperLogLog.class);
        }).toList();

        HyperLogLog mergedRegisters = new HyperLogLog(hyperLogLogs.toArray(new HyperLogLog[0]));
        
        store.setStoreValue(destKey, mergedRegisters);
        return OK_RESPONSE;

    }
    
}
