package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.Pair;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.timeseries.TimeSeries;

public class TsGetCommand extends AbstractCommand<Pair<Long, Double>> {

    public TsGetCommand() {
        super("TS.GET", 1, 1);
    }

    @Override
    protected Pair<Long, Double> processCommand(StorePrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);

        TimeSeries timeSeries = store.getStoreValue(key, TimeSeries.class);
        if (timeSeries == null) {
            return new Pair<>();
        }

        return timeSeries.get();
    }
    
}
