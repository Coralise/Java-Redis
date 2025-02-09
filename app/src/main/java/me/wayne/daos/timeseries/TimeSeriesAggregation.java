package me.wayne.daos.timeseries;

public enum TimeSeriesAggregation {
    AVG,    // Arithmetic mean of all values
    SUM,    // Sum of all values
    MIN,    // Minimum value
    MAX,    // Maximum value
    RANGE,  // Difference between the maximum and the minimum value
    COUNT,  // Number of values
    FIRST,  // Value with lowest timestamp in the bucket
    LAST,   // Value with highest timestamp in the bucket
    STD_P,  // Population standard deviation of the values
    STD_S,  // Sample standard deviation of the values
    VAR_P,  // Population variance of the values
    VAR_S,  // Sample variance of the values
    TWA     // Time-weighted average over the bucket's timeframe (since RedisTimeSeries v1.8)
}