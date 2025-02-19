package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.timeseries.DuplicatePolicy;
import me.wayne.daos.storevalues.timeseries.TimeSeries;

public class TsAddCommand extends AbstractCommand<Object> {

    public TsAddCommand() {
        super("TS.ADD", 3);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        long timestamp = args.get(1).equals("*") ? System.currentTimeMillis() : Long.parseLong(args.get(1));
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
        }

        Object res = timeSeries.add(timestamp, value);
        store.setStoreValue(key, timeSeries, "TS.ADD " + key + " " + timestamp + " " + value);
        return res;
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
