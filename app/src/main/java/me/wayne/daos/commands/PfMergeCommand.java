package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.probabilistic.HyperLogLog;
import me.wayne.daos.storevalues.StoreValue;

public class PfMergeCommand extends AbstractCommand<String> {

    public PfMergeCommand() {
        super("PFMERGE", 2);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        
        String destKey = args.get(0);
        List<HyperLogLog> hyperLogLogs = args.subList(1, args.size()).stream().map(key -> {
            StoreValue storeValue = store.getStoreValue(key);
            return storeValue == null ? new HyperLogLog() : storeValue.getValue(HyperLogLog.class);
        }).toList();

        HyperLogLog mergedRegisters = new HyperLogLog(hyperLogLogs.toArray(new HyperLogLog[0]));
        
        store.setStoreValue(destKey, mergedRegisters, inputLine);
        return OK_RESPONSE;

    }
    
}
