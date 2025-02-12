package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.io.StorePrintWriter;

public class GetBitCommand extends AbstractCommand<Integer> {

    public GetBitCommand() {
        super("GETBIT", 2, 2);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        int offset = Integer.parseInt(args.get(1));

        String value = store.getStoreValue(key, String.class);
        if (value == null) return 0;

        int byteIndex = offset / 8;
        int bitIndex = offset % 8;
        if (byteIndex >= value.length()) return 0;

        byte b = (byte) value.charAt(byteIndex);
        return (b >> (7 - bitIndex)) & 1;
    }
    
}
