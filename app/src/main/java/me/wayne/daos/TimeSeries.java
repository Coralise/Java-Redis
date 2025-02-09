package me.wayne.daos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import me.wayne.AssertUtil;

public class TimeSeries {

    private final String key;
    private final DuplicatePolicy duplicatePolicy;
    private final long retentionTime;
    private final Map<String, String> labels;
    private final TreeMap<Long, Double> values;
    private final long ignoreMaxTimeDiff;
    private final double ignoreMaxValDiff;

    public TimeSeries(
        String key,
        DuplicatePolicy duplicatePolicy,
        long retentionTime,
        Map<String,
        String> labels,
        long ignoreMaxTimeDiff,
        double ignoreMaxValDiff) {
        this.key = key;
        this.duplicatePolicy = duplicatePolicy != null ? duplicatePolicy : DuplicatePolicy.BLOCK;
        this.labels = labels;
        this.retentionTime = retentionTime;
        values = new TreeMap<>();
        this.ignoreMaxTimeDiff = ignoreMaxTimeDiff;
        this.ignoreMaxValDiff = ignoreMaxValDiff;
    }

    public Object add(double value) {
        return add(0, value);
    }

    public Object add(long timestamp, double value) {
        if (timestamp <= 0) timestamp = System.currentTimeMillis();

        Long lastTimestamp;
        if (values.isEmpty()) {
            lastTimestamp = null;
        } else {
            lastTimestamp = values.lastKey();
        }

        AssertUtil.assertTrue(retentionTime <= 0 || lastTimestamp == null || (lastTimestamp - retentionTime <= timestamp), "ERROR: Timestamp is past the retention time: " + timestamp);

        if (values.containsKey(timestamp)) {
            switch (duplicatePolicy) {
                case BLOCK:
                    return new int[0];
                case SUM:
                    values.put(timestamp, values.get(timestamp) + value);
                    return timestamp;
                case MIN:
                    values.put(timestamp, Math.min(values.get(timestamp), value));
                    return timestamp;
                case MAX:
                    values.put(timestamp, Math.max(values.get(timestamp), value));
                    return timestamp;
                case FIRST:
                    return new int[0];
                case LAST:
                    
                    if (ignoreMaxTimeDiff > 0 && ignoreMaxValDiff > 0 
                        && !values.isEmpty()
                        && timestamp >= values.lastKey()
                        && timestamp - values.lastKey() <= ignoreMaxTimeDiff
                        && (Math.abs(value - values.lastEntry().getValue())) <= ignoreMaxValDiff) return values.lastKey();

                    values.put(timestamp, value);
                    return timestamp;
                default:
                    return new int[0];
            }
        } else {
            values.put(timestamp, value);
            return timestamp;
        }
    }

    public List<Pair<Long, Double>> range(
        long from,
        long to,
        List<Long> tsFilter,
        List<Double> valueFilter,
        int count,
        String align,
        TimeSeriesAggregation aggregation,
        long bucketDuration,
        String bucketTimestamp,
        boolean includeEmptyBuckets) {

        ArrayList<Pair<Long, Double>> range = new ArrayList<>();

        if (from <= 0) from = values.firstKey();
        if (to <= 0) to = values.lastKey();

        int currentCount = 0;
        for (Map.Entry<Long, Double> entry : values.subMap(from, true, to, true).entrySet()) {
            if ((count > 0 && currentCount >= count) || 
                (tsFilter != null && !tsFilter.contains(entry.getKey())) || 
                (valueFilter != null && !valueFilter.contains(entry.getValue()))) {
                break;
            }

            range.add(new Pair<>(entry.getKey(), entry.getValue()));
            currentCount++;
        }

        if (aggregation != null) {
            long alignTimestamp;
            if (align != null) {
                if (align.equalsIgnoreCase("start") || align.equals("-")) {
                    alignTimestamp = from;
                } else if (align.equalsIgnoreCase("end") || align.equals("+")) {
                    long rangeSize = to - from;
                    long numBuckets = (long) Math.ceil(rangeSize / (double) bucketDuration);
                    alignTimestamp = to - numBuckets * bucketDuration;
                } else if (align.matches("\\d+")) {
                    alignTimestamp = Long.parseLong(align);
                } else {
                    throw new IllegalArgumentException("ERROR: Unknown ALIGN option: " + align);
                }
            } else {
                alignTimestamp = from;
            }
            if (bucketTimestamp == null) bucketTimestamp = "-";

            TreeSet<TimeSeriesBucket> aggregated = new TreeSet<>();

            while (alignTimestamp <= to) {
                TimeSeriesBucket bucket = new TimeSeriesBucket(alignTimestamp, bucketDuration);
                if (aggregated.contains(bucket)) bucket = aggregated.floor(bucket);

                for (Map.Entry<Long, Double> entry : values.subMap(alignTimestamp, true, alignTimestamp + bucketDuration, false).entrySet()) {
                    long timestamp = entry.getKey();
                    double value = entry.getValue();
                    bucket.add(timestamp, value);
                }
                if (bucket.hasEntries()) {
                    aggregated.add(bucket);
                } else if (includeEmptyBuckets) {
                    aggregated.add(bucket);
                }

                alignTimestamp += bucketDuration;
            }

            final String fBucketTimestamp = bucketTimestamp;
            
            return aggregated.stream().map(bucket -> bucket.getEntry(aggregation, fBucketTimestamp)).toList();
        } else {
            return range;
        }

    }

    public Pair<Long, Double> get() {
        return !values.isEmpty() ? new Pair<>(values.lastKey(), values.lastEntry().getValue()) : new Pair<>(null, null);
    }

}