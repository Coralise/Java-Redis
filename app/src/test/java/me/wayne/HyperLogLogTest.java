package me.wayne;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import me.wayne.daos.HyperLogLog;

class HyperLogLogTest {

    @ParameterizedTest
    @ValueSource(ints = {10000, 20000, 35000, 42000, 57000, 73829, 90423, 101000, 150000, 200000})
    void hyperLogLogTest(int count) {
        HyperLogLog hyperLogLog = new HyperLogLog(16);
        for (int i = 0; i < count; i++) {
            hyperLogLog.add("Test-" + i);
        }
        assertEquals(count, hyperLogLog.estimate(), count * 0.02);
    }
}
