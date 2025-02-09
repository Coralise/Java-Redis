package me.wayne;

import me.wayne.daos.timeseries.DuplicatePolicy;
import me.wayne.daos.timeseries.TimeSeries;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TimeSeriesTest {

    @Test
    void testAddValue() {
        Map<String, String> labels = new HashMap<>();
        TimeSeries timeSeries = new TimeSeries("testKey", DuplicatePolicy.SUM, 1000L, labels, 0L, 0.0);
        Object result = timeSeries.add(System.currentTimeMillis(), 10.0);
        assertNotNull(result);
        assertTrue(result instanceof Long);
    }

    @Test
    void testAddDuplicateValue() {
        Map<String, String> labels = new HashMap<>();
        TimeSeries timeSeries = new TimeSeries("testKey", DuplicatePolicy.BLOCK, 1000L, labels, 0L, 0.0);
        long timestamp = System.currentTimeMillis();
        timeSeries.add(timestamp, 10.0);
        Object result = timeSeries.add(timestamp, 20.0);
        assertTrue(result instanceof int[]);
    }

    @Test
    void testAddValuePastRetentionTime() {
        Map<String, String> labels = new HashMap<>();
        TimeSeries timeSeries = new TimeSeries("testKey", DuplicatePolicy.SUM, 1000L, labels, 0L, 0.0);
        timeSeries.add(20);
        long timestamp = System.currentTimeMillis() - 2000L;
        AssertionError exception = assertThrows(AssertionError.class, () -> {
            timeSeries.add(timestamp, 10.0);
        });
        assertEquals("ERROR: Timestamp is past the retention time: " + timestamp, exception.getMessage());
    }

}
