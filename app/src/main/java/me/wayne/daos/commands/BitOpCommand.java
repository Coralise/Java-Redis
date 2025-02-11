package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.StoreValue;

public class BitOpCommand extends AbstractCommand<Integer> {

    public BitOpCommand() {
        super("BITOP", 3);
    }

    @Override
    protected Integer processCommand(PrintWriter out, InMemoryStore store, List<String> args) {

        String operation = args.get(0).toUpperCase();
        String destKey = args.get(1);
        List<String> keys = args.subList(2, args.size());

        if (operation.equals("NOT")) {
            if (keys.size() != 1) throw new IllegalArgumentException("ERROR: BITOP NOT takes only one input key");
            String key = keys.get(0);
            StoreValue storeValue = store.getStoreValue(key, true);
            byte[] bytes = storeValue.getValue(String.class).getBytes();
            int[] bits = toBitArray(bytes);
            int[] reversedBits = new int[bits.length];
            for (int i = 0; i < bits.length; i++) reversedBits[i] = bits[i] == 0 ? 1 : 0;
            byte[] reversedBytes = bitsToBytes(reversedBits);
            store.setStoreValue(destKey, new String(reversedBytes));
            return reversedBytes.length;
        }

        int[] combinedBits = new int[0];
        ArrayList<int[]> bitArrays = new ArrayList<>();

        for (String key : keys) {
            StoreValue storeValue = store.getStoreValue(key);
            if (storeValue == null || !storeValue.isInstanceOfClass(String.class)) continue;
            byte[] bytes = storeValue.getValue(String.class).getBytes();
            int[] bits = toBitArray(bytes);
            if (bits.length > combinedBits.length) {
                combinedBits = Arrays.copyOf(combinedBits, bits.length);
                bitArrays.addFirst(bits);
            } else bitArrays.add(bits);
        }

        for (int i = 0; i < combinedBits.length; i++) {

            int current = bitArrays.get(0)[i];
            for (int j = 1; j < bitArrays.size(); j++) {
                int[] bits = bitArrays.get(j);
                switch (operation) {
                    case "AND":
                        if (i >= bits.length) current &= 0;
                        else current &= bits[i];
                        break;
                    case "OR":
                        if (i < bits.length) current |= bits[i];
                        break;
                    case "XOR":
                        if (i < bits.length) current ^= bits[i];
                        break;
                    default:
                        throw new IllegalArgumentException("ERROR: Unknown operation: " + operation);
                }
            }
            combinedBits[i] = current;

        }

        byte[] combinedBytes = bitsToBytes(combinedBits);
        store.setStoreValue(destKey, new String(combinedBytes));
        return combinedBytes.length;
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

    public static byte[] bitsToBytes(int[] bits) {
        if (bits.length % 8 != 0) {
            throw new IllegalArgumentException("Bit array length must be a multiple of 8");
        }

        byte[] byteArray = new byte[bits.length / 8];

        for (int i = 0; i < byteArray.length; i++) {
            byte value = 0;
            for (int j = 0; j < 8; j++) {
                value = (byte) ((value << 1) | bits[i * 8 + j]); // Shift and OR operation
            }
            byteArray[i] = value;
        }

        return byteArray;
    }
    
}
