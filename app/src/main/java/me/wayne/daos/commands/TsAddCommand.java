package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.wayne.InMemoryStore;
import me.wayne.daos.timeseries.DuplicatePolicy;
import me.wayne.daos.timeseries.TimeSeries;

public class TsAddCommand extends AbstractCommand<Object> {

    public TsAddCommand() {
        super("TS.ADD", 3);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object processCommand(PrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        long timestamp = args.get(1).equals("*") ? 0 : Long.parseLong(args.get(1));
        double value = Double.parseDouble(args.get(2));

        TimeSeries timeSeries = store.getStoreValue(key, TimeSeries.class);
        if (timeSeries == null) {
            Map<String, Object> options = parseOptions(args.subList(2, args.size()));
            long retentionPeriod = (long) options.getOrDefault("retentionPeriod", 0L);
            DuplicatePolicy duplicatePolicy = (DuplicatePolicy) options.getOrDefault("duplicatePolicy", DuplicatePolicy.BLOCK);
            long ignoreMaxTimeDiff = (long) options.getOrDefault("ignoreMaxTimeDiff", 0L);
            double ignoreMaxValDiff = (double) options.getOrDefault("ignoreMaxValDiff", 0.0);
            Map<String, String> labels = (Map<String, String>) options.getOrDefault("labels", new HashMap<>());
    
            timeSeries = new TimeSeries(key, duplicatePolicy, retentionPeriod, labels, ignoreMaxTimeDiff, ignoreMaxValDiff);
            store.setStoreValue(key, timeSeries);
        }

        return timeSeries.add(timestamp, value);
    }

    @SuppressWarnings("all")
    private Map<String, Object> parseOptions(List<String> args) {
        Map<String, Object> options = new HashMap<>();
        options.put("key", args.get(0));

        for (int i = 1; i < args.size(); i++) {
            switch (args.get(i).toUpperCase()) {
                case "RETENTION":
                    options.put("retentionPeriod", Long.parseLong(args.get(++i)));
                    break;
                case "DUPLICATE_POLICY":
                    options.put("duplicatePolicy", DuplicatePolicy.valueOf(args.get(++i).toUpperCase()));
                    break;
                case "IGNORE":
                    options.put("ignoreMaxTimeDiff", Long.parseLong(args.get(++i)));
                    options.put("ignoreMaxValDiff", Double.parseDouble(args.get(++i)));
                    break;
                case "LABELS":
                    Map<String, String> labels = new HashMap<>();
                    while (i + 1 < args.size() && !args.get(i + 1).startsWith("[")) {
                        labels.put(args.get(++i), args.get(++i));
                    }
                    options.put("labels", labels);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown option: " + args.get(i));
            }
        }

        return options;
    }
    
}
