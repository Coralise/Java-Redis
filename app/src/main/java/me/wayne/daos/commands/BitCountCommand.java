package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.io.StorePrintWriter;

public class BitCountCommand extends AbstractCommand<Integer> {

    public BitCountCommand() {
        super("BITCOUNT", 1, 4);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        String value = store.getStoreValue(key, String.class);
        if (value == null || value.isEmpty()) return 0;
        int start = 0;
        int end = value.length() - 1;
        if (args.size() >= 3) {
            start = Integer.parseInt(args.get(1));
            while (start < 0) start += value.length();
            end = Integer.parseInt(args.get(2));
            while (end < 0) end += value.length();
        }
        String type = args.size() == 4 ? args.get(3) : "BYTE";

        byte[] bytes = value.getBytes();
        if ("BYTE".equals(type)) {
            byte[] bytesToCount = Arrays.copyOfRange(bytes, start, end + 1);
            int bitCount = 0;
            for (byte b : bytesToCount) bitCount += Integer.bitCount(b & 0xFF);
            return bitCount;
        } else if ("BIT".equals(type)) {
            int[] bitArray = Arrays.copyOfRange(toBitArray(bytes), start, end + 1);
            int bitCount = 0;
            for (int bit : bitArray) bitCount += bit;
            return bitCount;
        } else {
            throw new IllegalArgumentException("ERROR: Bit type is not BYTE or BIT");
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
