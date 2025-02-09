package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.HyperLogLog;

public class PfAddCommand extends AbstractCommand<Integer> {

    public PfAddCommand() {
        super("PFADD", 2);
    }

    @Override
    protected Integer processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        List<String> values = args.subList(1, args.size());

        HyperLogLog hyperLogLog = store.getObject(key, HyperLogLog.class);
        if (hyperLogLog == null) hyperLogLog = new HyperLogLog(15);

        boolean updated = false;
        double oldEstimate = hyperLogLog.estimate();
        for (String value : values) {
            hyperLogLog.add(value);
            if (!updated) updated = oldEstimate != hyperLogLog.estimate();
        }

        store.getStore().put(key, hyperLogLog);
        return updated ? 1 : 0;
    }
    
}
