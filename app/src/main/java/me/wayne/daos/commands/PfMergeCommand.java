package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.HyperLogLog;

public class PfMergeCommand extends AbstractCommand<String> {

    public PfMergeCommand() {
        super("PFMERGE", 2);
    }

    @Override
    protected String processCommand(Thread thread, InMemoryStore store, List<String> args) {
        
        String destKey = args.get(0);
        List<HyperLogLog> hyperLogLogs = args.subList(1, args.size()).stream().map(key -> store.getObject(key, HyperLogLog.class)).toList();

        HyperLogLog mergedRegisters = new HyperLogLog(hyperLogLogs.toArray(new HyperLogLog[0]));
        
        store.getStore().put(destKey, mergedRegisters);
        return OK_RESPONSE;

    }
    
}
