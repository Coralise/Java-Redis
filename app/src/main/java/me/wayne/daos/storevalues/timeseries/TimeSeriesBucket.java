package me.wayne.daos.storevalues.timeseries;

import java.io.Serializable;
import java.util.TreeMap;

import me.wayne.AssertUtil;
import me.wayne.daos.Pair;

public class TimeSeriesBucket implements Comparable<TimeSeriesBucket>, Serializable {
    private static final long serialVersionUID = 1L;

    private final Long bucketStart;
    private final Long bucketDuration;
    private final TreeMap<Long, Double> data = new TreeMap<>();

    public TimeSeriesBucket(long bucketStart, long bucketDuration) {
        this.bucketStart = bucketStart;
        this.bucketDuration = bucketDuration;
    }
    
    public long getBucketStart() {
        return bucketStart;
    }

    public long getBucketEnd() {
        return bucketStart + bucketDuration;
    }

    public void add(long timestamp, double value) {
        AssertUtil.assertTrue(timestamp >= bucketStart && timestamp <= getBucketEnd(), "ERROR: Timestamp is not within the bucket");
        data.put(timestamp, value);
    }
    
    @Override
    public int compareTo(TimeSeriesBucket o) {
        return bucketStart.compareTo(o.bucketStart);
    } 

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bucketStart == null) ? 0 : bucketStart.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TimeSeriesBucket other = (TimeSeriesBucket) obj;
        if (bucketStart == null) {
            if (other.bucketStart != null)
                return false;
        } else if (!bucketStart.equals(other.bucketStart))
            return false;
        return true;
    }

    public Pair<Long, Double> getEntry(TimeSeriesAggregation aggregation, String bucketTimestamp) {
        AssertUtil.assertTrue(bucketTimestamp.equals("-") || bucketTimestamp.equals("+") || bucketTimestamp.equals("~"), "ERROR: Invalid bucket timestamp");
        long timestampToDisplay;
        switch (bucketTimestamp) {
            case "-":
                timestampToDisplay = bucketStart;
                break;
            case "+":
                timestampToDisplay = getBucketEnd();
                break;
            case "~":
                timestampToDisplay = bucketStart + (bucketDuration / 2);
                break;
            default:
                timestampToDisplay = bucketStart;
        }
        if (data.isEmpty()) {
            return new Pair<>(timestampToDisplay, 0.0);
        }
        switch (aggregation) {
            case AVG:
                return new Pair<>(timestampToDisplay, data.values().stream().mapToDouble(Double::doubleValue).average().orElse(0));
            case SUM:
                return new Pair<>(timestampToDisplay, data.values().stream().mapToDouble(Double::doubleValue).sum());
            case MIN:
                return new Pair<>(timestampToDisplay, data.values().stream().mapToDouble(Double::doubleValue).min().orElse(0));
            case MAX:
                return new Pair<>(timestampToDisplay, data.values().stream().mapToDouble(Double::doubleValue).max().orElse(0));
            case RANGE:
                double min = data.values().stream().mapToDouble(Double::doubleValue).min().orElse(0);
                double max = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
                return new Pair<>(timestampToDisplay, max - min);
            case COUNT:
                return new Pair<>(timestampToDisplay, (double) data.size());
            case FIRST:
                return new Pair<>(timestampToDisplay, data.firstEntry().getValue());
            case LAST:
                return new Pair<>(timestampToDisplay, data.lastEntry().getValue());
            case STD_P:
                double mean = data.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double variance = data.values().stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0.0);
                double stdDev = Math.sqrt(variance);
                return new Pair<>(timestampToDisplay, stdDev);
            case STD_S:
                double sampleMean = data.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double sampleVariance = data.values().stream().mapToDouble(v -> Math.pow(v - sampleMean, 2)).sum() / (data.size() - 1);
                double sampleStdDev = Math.sqrt(sampleVariance);
                return new Pair<>(timestampToDisplay, sampleStdDev);
            case VAR_P:
                double popMean = data.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double popVariance = data.values().stream().mapToDouble(v -> Math.pow(v - popMean, 2)).average().orElse(0.0);
                return new Pair<>(timestampToDisplay, popVariance);
            case VAR_S:
                double sampleVarMean = data.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double sampleVar = data.values().stream().mapToDouble(v -> Math.pow(v - sampleVarMean, 2)).sum() / (data.size() - 1);
                return new Pair<>(timestampToDisplay, sampleVar);
            case TWA:
                double totalWeightedValue = data.entrySet().stream()
                    .mapToDouble(entry -> entry.getValue() * (entry.getKey() - bucketStart))
                    .sum();
                double totalTime = (double) data.lastKey() - bucketStart;
                double twa = totalWeightedValue / totalTime;
                return new Pair<>(timestampToDisplay, twa);
            default:
                throw new IllegalArgumentException("ERROR: Unknown aggregation type");
        }
    }

    public boolean hasEntries() {
        return !data.isEmpty();
    }
    
}
