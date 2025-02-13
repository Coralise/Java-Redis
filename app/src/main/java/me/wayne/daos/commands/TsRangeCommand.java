package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.wayne.daos.Pair;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.timeseries.TimeSeries;
import me.wayne.daos.storevalues.timeseries.TimeSeriesAggregation;

public class TsRangeCommand extends AbstractCommand<List<Pair<Long, Double>>> {

    public TsRangeCommand() {
        super("TS.RANGE", 3);
    }

    @SuppressWarnings("all")
    @Override
    protected List<Pair<Long, Double>> processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        long start = args.get(1).equals("-") ? 0 : Long.parseLong(args.get(1));
        long end = args.get(2).equals("+") ? Long.MAX_VALUE : Long.parseLong(args.get(2));

        TimeSeries timeSeries = store.getStoreValue(key, TimeSeries.class);
        if (timeSeries == null) return new ArrayList<>();

        Map<String, Object> options = parseOptions(args.subList(3, args.size()));

        return timeSeries.range(
            start,
            end,
            options.containsKey("FILTER_BY_TS") ? (List<Long>) options.get("FILTER_BY_TS") : null,
            options.containsKey("FILTER_BY_VALUE") ? List.of(((double[]) options.get("FILTER_BY_VALUE"))[0], ((double[]) options.get("FILTER_BY_VALUE"))[1]) : null,
            options.containsKey("COUNT") ? (Integer) options.get("COUNT") : 0,
            options.containsKey("ALIGN") ? (String) options.get("ALIGN") : null,
            options.containsKey("AGGREGATION") ? TimeSeriesAggregation.valueOf(((String) ((Object[]) options.get("AGGREGATION"))[0]).toUpperCase()) : null,
            options.containsKey("AGGREGATION") ? (Long) ((Object[]) options.get("AGGREGATION"))[1] : 0,
            options.containsKey("BUCKETTIMESTAMP") ? (String) options.get("BUCKETTIMESTAMP") : null,
            options.containsKey("EMPTY") ? (Boolean) options.get("EMPTY") : false);
    }

    private Map<String, Object> parseOptions(List<String> args) {
        Map<String, Object> options = new HashMap<>();
        int i = 0;

        while (i < args.size()) {
            String arg = args.get(i).toUpperCase();

            switch (arg) {
                case "FILTER_BY_TS":
                    List<Long> timestamps = new ArrayList<>();
                    i++;
                    while (i < args.size() && isNumeric(args.get(i))) {
                        timestamps.add(Long.parseLong(args.get(i)));
                        i++;
                    }
                    options.put("FILTER_BY_TS", timestamps);
                    break;
                case "FILTER_BY_VALUE":
                    double min = Double.parseDouble(args.get(i + 1));
                    double max = Double.parseDouble(args.get(i + 2));
                    options.put("FILTER_BY_VALUE", new double[]{min, max});
                    i += 3;
                    break;
                case "COUNT":
                    int count = Integer.parseInt(args.get(i + 1));
                    options.put("COUNT", count);
                    i += 2;
                    break;
                case "ALIGN":
                    String align = args.get(i + 1);
                    options.put("ALIGN", align);
                    i += 2;
                    break;
                case "AGGREGATION":
                    String aggregator = args.get(i + 1);
                    long bucketDuration = Long.parseLong(args.get(i + 2));
                    options.put("AGGREGATION", new Object[]{aggregator, bucketDuration});
                    i += 3;
                    if (i < args.size() && args.get(i).equalsIgnoreCase("BUCKETTIMESTAMP")) {
                        String bucketTimestamp = args.get(i + 1);
                        options.put("BUCKETTIMESTAMP", bucketTimestamp);
                        i += 2;
                    }
                    if (i < args.size() && args.get(i).equalsIgnoreCase("EMPTY")) {
                        options.put("EMPTY", true);
                        i++;
                    }
                    break;
                default:
                    i++;
                    break;
            }
        }

        return options;
    }

    private boolean isNumeric(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
}
