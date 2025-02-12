package me.wayne.daos.commands;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import me.wayne.AssertUtil;
import me.wayne.daos.StoreValue;
import me.wayne.daos.io.StorePrintWriter;

public class SetBitCommand extends AbstractCommand<Integer> {

    public SetBitCommand() {
        super("SETBIT", 3);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        int offset = Integer.parseInt(args.get(1));
        int bitValue = Integer.parseInt(args.get(2));

        AssertUtil.assertTrue(bitValue == 0 || bitValue == 1, "ERROR: bit value must be 0 or 1");

        byte[] bytes;
        StoreValue storeValue = store.getStoreValue(key);
        bytes = storeValue == null ? new byte[(offset / 8) + 1] : storeValue.getValue(String.class).getBytes();

        logger.log(Level.INFO, "BitSet String: {0}", new String(bytes));
        logger.log(Level.INFO, "BitSet: {0}", bitSetToBinaryString(bytes));
        int previousBit = (offset < bytes.length * 8) ? ((bytes[offset / 8] >> (7 - (offset % 8))) & 1) : 0;
        bytes = setBit(bytes, offset, bitValue);
        logger.log(Level.INFO, "BitSet: {0}", bitSetToBinaryString(bytes));

        store.setStoreValue(key, new String(bytes));
        return previousBit;
    }
    
    private byte[] setBit(byte[] byteArray, int bitIndex, int value) {
        int bytePos = bitIndex / 8;  // Find the byte position
        int bitPos = bitIndex % 8;   // Find the bit position in the byte

        // Expand byte array if needed
        logger.log(Level.INFO, "Byte Array Length: {0} - {1}", new Object[]{byteArray.length, bytePos});
        if (bytePos >= byteArray.length) byteArray = Arrays.copyOf(byteArray, bytePos + 1);
        logger.log(Level.INFO, "Byte Array Length: {0}", byteArray.length);

        if (value == 1) {
            byteArray[bytePos] |= (1 << (7 - bitPos));  // Set bit to 1
        } else {
            byteArray[bytePos] &= ~(1 << (7 - bitPos)); // Set bit to 0
        }

        return byteArray;
    }

    private String bitSetToBinaryString(byte[] byteArray) {
        StringBuilder binaryString = new StringBuilder();
        for (byte b : byteArray) {
            for (int i = 7; i >= 0; i--) {
            binaryString.append((b >> i) & 1);
            }
        }
        return binaryString.toString();
    }
    
}
