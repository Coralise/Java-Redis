package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.StoreValue;
import me.wayne.daos.probabilistic.HyperLogLog;

public class PfCountCommand extends AbstractCommand<Integer> {

    public PfCountCommand() {
        super("PFCOUNT", 1);
    }

    @Override
    protected Integer processCommand(PrintWriter out, InMemoryStore store, List<String> args) {
        List<HyperLogLog> hyperLogLogs = args.subList(0, args.size()).stream().map(key -> {
            StoreValue storeValue = store.getStoreValue(key);
            return storeValue == null ? new HyperLogLog(15) : storeValue.getValue(HyperLogLog.class);
        }).toList();

        if (hyperLogLogs.size() == 1) return (int) hyperLogLogs.get(0).estimate();
        
        HyperLogLog mergedHyperLogLog = new HyperLogLog(hyperLogLogs.toArray(new HyperLogLog[0]));
        return (int) mergedHyperLogLog.estimate();
    }
    
}
