package me.wayne;

import java.util.Arrays;
import java.util.BitSet;

public class AssertUtil {

    private AssertUtil() {
    }
    
    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
    public static void main(String[] args) {
        // Step 1: Create a byte array large enough to hold the highest bit position (14)
        byte[] byteArray = new byte[2]; // Redis used 2 bytes (16 bits)

        // Step 2: Set bits manually (mimicking Redis' SETBIT)
        setBit(byteArray, 2);
        setBit(byteArray, 3);
        setBit(byteArray, 5);
        setBit(byteArray, 10);
        setBit(byteArray, 11);
        setBit(byteArray, 14);

        // Step 3: Convert the byte array to a string (Redis-like output)
        String redisString = new String(byteArray, java.nio.charset.StandardCharsets.ISO_8859_1);

        // Step 4: Print results
        System.out.println("BitSet as Redis-like String: " + redisString);
        System.out.println("BitSet as Bytes: " + Arrays.toString(byteArray));
    }

    // Helper method to set a bit at a given offset in the byte array
    private static void setBit(byte[] byteArray, int bitIndex) {
        int bytePos = bitIndex / 8;  // Find the byte position
        int bitPos = bitIndex % 8;   // Find the bit position in the byte

        byteArray[bytePos] |= (1 << (7 - bitPos)); // Set bit in big-endian order
    }

}
