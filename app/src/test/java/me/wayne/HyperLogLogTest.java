package me.wayne;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import me.wayne.daos.storevalues.probabilistic.HyperLogLog;

class HyperLogLogTest {

    @ParameterizedTest
    @ValueSource(ints = {
        1,
        2,
        5,
        7,
        10,
        15,
        32,
        40,
        123,
        195,
        395,
        10000,
        20000,
        35000,
        42000,
        57000,
        73829,
        90423,
        101000,
        150000,
        200000,
        250000,
        300000,
        400000,
        500000,
        600000,
        610000,
        620000,
        630000,
        640000,
        645000,
        648000,
        648500,
        648945,
        649000,
        650000,
        700000,
        800000,
        900000,
        1000000,
        2000000,
        3000000,
        4000000,
        5000000,
        6000000,
        7000000,
        8000000,
        9000000,
        10000000,
        20000000,
        30000000,
        40000000,
        50000000,
        60000000,
        70000000,
        80000000,
        90000000,
        100000000
    })
    void hyperLogLogTest(int count) {
        HyperLogLog hyperLogLog = new HyperLogLog();
        for (int i = 0; i < count; i++) {
            hyperLogLog.add("Test-" + i);
        }
        assertEquals(count, hyperLogLog.estimate(), count * 0.02);
    }
    
}
