package me.wayne.daos;

import java.math.BigDecimal;
import java.math.MathContext;

import me.wayne.AssertUtil;

public class HyperLogLog {
    private final int precision;
    private final byte[] registers;

    public HyperLogLog(int precision) {
        this.precision = precision;
        AssertUtil.assertTrue(precision >= 4 && precision <= 16, "Precision must be between 4 and 16");
        registers = new byte[(int) Math.pow(2, precision)];
    }

    public int add(String string) {
        int murmurHash = MurmurHash.hash32(string);
        int registerIndex = getRegisterIndex(murmurHash);
        int rank = countTrailingZeros(murmurHash);
        registers[registerIndex] = (byte) Math.max(registers[registerIndex], rank);
        return rank;
    }

    private int countTrailingZeros(int hash) {
        return Integer.numberOfTrailingZeros(hash) + 1;
    }

    private int getRegisterIndex(int hash) {
        return hash >>> (32 - precision);
    }

    private double alpha() {
        if (registers.length < 32) return 0.673;
        if (registers.length < 64) return 0.697;
        if (registers.length < 128) return 0.709;
        return 0.7213 / (1 + 1.079 / registers.length);
    }

    public double estimate() {
        BigDecimal sum = BigDecimal.ZERO;
        int zeroCount = 0;

        for (byte register : registers) {
            sum = sum.add(BigDecimal.ONE.divide(BigDecimal.valueOf(1L << register), MathContext.DECIMAL128));
            if (register == 0) zeroCount++;
        }
        int registerCount = registers.length;
        if (sum == BigDecimal.ZERO) return 0;

        double alpha = alpha();
        double rawEstimate = alpha * (registerCount * registerCount) / sum.doubleValue();

        if (rawEstimate <= 2.5 * registerCount) {
            if (zeroCount > 0) {
                return registerCount * Math.log((double) registerCount / (double) zeroCount);
            } else return rawEstimate;
        } else if (rawEstimate > (1L << 32) / 30.0) {
            return -(1L << 32) * Math.log(1.0 - (rawEstimate / (1L << 32)));
        } else {
            return rawEstimate;
        }

    }

    public int[] toBitArray(byte[] byteArray) {
        int[] bitArray = new int[byteArray.length * 8];
        int index = 0;
        
        for (byte b : byteArray) {
            for (int i = 7; i >= 0; i--) {
                bitArray[index++] = (b >> i) & 1;
            }
        }
        return bitArray;
    }
}
