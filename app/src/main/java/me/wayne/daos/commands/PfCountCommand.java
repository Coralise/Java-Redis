package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.probabilistic.HyperLogLog;
import me.wayne.daos.storevalues.StoreValue;

public class PfCountCommand extends AbstractCommand<Integer> {

    public PfCountCommand() {
        super("PFCOUNT", 1);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        List<HyperLogLog> hyperLogLogs = args.subList(0, args.size()).stream().map(key -> {
            StoreValue storeValue = store.getStoreValue(key);
            return storeValue == null ? new HyperLogLog() : storeValue.getValue(HyperLogLog.class);
        }).toList();

        if (hyperLogLogs.size() == 1) return (int) hyperLogLogs.get(0).estimate();
        
        HyperLogLog mergedHyperLogLog = new HyperLogLog(hyperLogLogs.toArray(new HyperLogLog[0]));
        return (int) mergedHyperLogLog.estimate();
    }
    
}
