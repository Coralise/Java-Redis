package me.wayne.daos.bitfields;

import java.util.Arrays;

import me.wayne.AssertUtil;

public class BitField {

    private int[] bitArray;

    public BitField() {
        this.bitArray = new int[0];
    }

    public String getBitArrayString() {
        return Arrays.toString(bitArray);
    }

    public Integer set(long value, int offset, int bits, boolean signed) {
        return set(value, offset, bits, signed, OverflowMode.WRAP);
    }

    public Integer set(long value, int offset, int bits, boolean signed, OverflowMode overflow) {
        if (bits < 1 || bits > 63) {
            throw new IllegalArgumentException("Bit width must be between 1 and 63.");
        }

        if (bitArray.length < offset + bits) {
            int[] newBitArray = new int[offset + bits];
            System.arraycopy(bitArray, 0, newBitArray, 0, bitArray.length);
            bitArray = newBitArray;
        }
        
        // Compute the max value range
        long maxValue = (1L << bits) - 1; // 1111 --- 15
        long minValue = signed ? -(1L << (bits - 1)) : 0; // if signed, -(1000) --- -8

        // Handle overflow for unsigned and signed values
        switch (overflow) {
            case WRAP:
                if (signed) {
                    if (value < minValue || value > maxValue / 2) { // XXXX... < -(1000) or XXXX... > 1111 / 2 --- v < -8 or v > 7
                        value = value & maxValue;  // Truncate to fit --- XXXX... & 1111 = XXXX
                    }
                } else {
                    value = value & maxValue; // Enforce unsigned range --- XXXX... & 1111 = XXXX
                }
                break;
            case SAT:
                if (signed) {
                    if (value < minValue) value = minValue;
                    else if (value > maxValue / 2) value = maxValue / 2;
                } else {
                    if (value < 0) value = 0;
                    else if (value > maxValue) value = maxValue;
                }
                break;
            case FAIL:
                if (signed) {
                    if (value < minValue || value > maxValue / 2) {
                        return null;
                    }
                } else {
                    if (value < 0 || value > maxValue) return null;
                }
                break;
        }

        int previousValue = getInt(offset, bits, signed); // Get the previous value
        // Convert to binary representation (LSB -> MSB)
        for (int i = offset; i < offset + bits; i++) { // 0 - 3
            bitArray[i] = (int) ((value >> (bits-1) - (i-offset)) & 1); // [0] = XXXX & 1 = X, [1] = 0XXX & 1 = X, [2] = 00XX & 1 = X, [3] = 000X & 1 = X
        }

        return previousValue;

    }

    public int getInt(int offset, int bits, boolean signed) {
        if (bits < 1 || bits > 63) {
            throw new IllegalArgumentException("Bit width must be between 1 and 63.");
        }
        if (offset < 0 || offset >= bitArray.length) return 0;

        int value = 0;
        for (int i = 0; i < bits; i++) { // 0 - 3
            int bit = (offset + bits-1) - i < bitArray.length ? bitArray[(offset + bits-1) - i] : 0;
            value |= bit << i; // [0] = X | 0 = X, [1] = X0 | 0X = XX, [2] = X00 | XX = XXX
        }

        if (signed && (value >> (bits - 1)) == 1) { // XXXX & 0001 = 000X
            int valToSubtract = (1 << bits); // 10000
            value = value < valToSubtract / 2 ? value : value - valToSubtract;
        }
        
        return value;
    }

    public Integer incrBy(long increment, int offset, int bits, boolean signed) {
        return incrBy(increment, offset, bits, signed, OverflowMode.WRAP);
    }
    
    public Integer incrBy(long increment, int offset, int bits, boolean signed, OverflowMode overflow) {
        int value = getInt(offset, bits, signed);
        return set(value + increment, offset, bits, signed, overflow);
    }

    public Integer getInt(String offset, String encoding) {
        boolean signed = getSigned(encoding);
        int bits = getBits(offset, encoding);
        int offsetInt = getOffsetInt(offset, bits);
        return getInt(offsetInt, bits, signed);
    }

    public Integer set(Long value, String offset, String encoding, OverflowMode overflow) {
        boolean signed = getSigned(encoding);
        int bits = getBits(offset, encoding);
        int offsetInt = getOffsetInt(offset, bits);
        return set(value, offsetInt, bits, signed, overflow);
    }

    public Integer incrBy(Long increment, String offset, String encoding, OverflowMode overflow) {
        boolean signed = getSigned(encoding);
        int bits = getBits(offset, encoding);
        int offsetInt = getOffsetInt(offset, bits);
        return incrBy(increment, offsetInt, bits, signed, overflow);
    }

    private int getOffsetInt(String offset, int bits) {
        return offset.startsWith("#") ? Integer.parseInt(offset.substring(1)) * bits : Integer.parseInt(offset);
    }

    private int getBits(String offset, String encoding) {
        AssertUtil.assertTrue(offset.matches("#?\\d+"), "Invalid offset: " + offset);
        return Integer.parseInt(encoding.substring(1));
    }

    private boolean getSigned(String encoding) {
        AssertUtil.assertTrue(encoding.startsWith("i") || encoding.startsWith("u"), "Invalid encoding: " + encoding);
        return encoding.charAt(0) == 'i';
    }
}
