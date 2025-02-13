package me.wayne;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import me.wayne.daos.storevalues.bitfields.BitField;
import me.wayne.daos.storevalues.bitfields.OverflowMode;

class BitFieldTest {
    
    @Test
    void bitFieldTest() {

        BitField bitField = new BitField();

        assertEquals(0, bitField.set(13, 13, 4, true));
        assertEquals(-3, bitField.set(13, 13, 4, true));
        assertEquals(-3, bitField.getInt(13, 4, true));
        assertEquals(6, bitField.set(5, 12, 4, true));
        assertEquals(5, bitField.getInt(12, 4, true));
        assertEquals(-5, bitField.getInt(13, 4, true));
        assertEquals(-4, bitField.getInt(15, 4, true));
        assertEquals(88, bitField.getInt(12, 8, true));
        assertEquals(0, bitField.set(20, 0, 4, true));
        assertEquals(4, bitField.set(20, 0, 4, true));
        assertEquals(4, bitField.set(20, 0, 4, true, OverflowMode.SAT));
        assertEquals(7, bitField.set(20, 0, 4, true, OverflowMode.SAT));
        assertEquals(null, bitField.set(20, 0, 4, true, OverflowMode.FAIL));
        assertEquals(null, bitField.set(8, 0, 4, true, OverflowMode.FAIL));
        assertEquals(7, bitField.set(7, 0, 4, true, OverflowMode.FAIL));
        assertEquals(7, bitField.set(-8, 0, 4, true, OverflowMode.SAT));
        assertEquals(-8, bitField.incrBy(-1, 0, 4, true, OverflowMode.SAT));
        assertEquals(-8, bitField.set(-8, 0, 4, true, OverflowMode.FAIL));
        assertEquals(null, bitField.incrBy(-1, 0, 4, true, OverflowMode.FAIL));
        
    }
}
