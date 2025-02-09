package me.wayne.daos.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.wayne.InMemoryStore;
import me.wayne.daos.TimeSeries;
import me.wayne.daos.DuplicatePolicy;

public class TsCreateCommand extends AbstractCommand<String> {

    public TsCreateCommand() {
        super("TS.CREATE", 1);
    }

    @Override
    protected String processCommand(Thread thread, InMemoryStore store, List<String> args) {
        Map<String, Object> options = parseOptions(args);
        String key = (String) options.get("key");
        long retentionPeriod = (long) options.getOrDefault("retentionPeriod", 0L);
        DuplicatePolicy duplicatePolicy = (DuplicatePolicy) options.getOrDefault("duplicatePolicy", DuplicatePolicy.BLOCK);
        long ignoreMaxTimeDiff = (long) options.getOrDefault("ignoreMaxTimeDiff", 0L);
        double ignoreMaxValDiff = (double) options.getOrDefault("ignoreMaxValDiff", 0.0);
        Map<String, String> labels = (Map<String, String>) options.getOrDefault("labels", new HashMap<>());

        store.getStore().put(key, new TimeSeries(key, duplicatePolicy, retentionPeriod, labels, ignoreMaxTimeDiff, ignoreMaxValDiff));
        return OK_RESPONSE;
    }

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
