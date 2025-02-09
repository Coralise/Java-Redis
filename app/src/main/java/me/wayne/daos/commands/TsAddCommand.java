package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.TimeSeries;

public class TsAddCommand extends AbstractCommand<Object> {

    public TsAddCommand() {
        super("TS.ADD", 3);
    }

    @Override
    protected Object processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        long timestamp = args.get(1).equals("*") ? 0 : Long.parseLong(args.get(1));
        double value = Double.parseDouble(args.get(2));

        TimeSeries timeSeries = store.getObject(key, TimeSeries.class);
        if (timeSeries == null) {
            new TsCreateCommand().executeCommand(thread, store, "TS.CREATE " + key + " " + String.join(" ", args.subList(3, args.size())));
            timeSeries = store.getObject(key, TimeSeries.class);
        }

        return timeSeries.add(timestamp, value);
    }
    
}
