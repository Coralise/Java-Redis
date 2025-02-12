package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import me.wayne.InMemoryStore;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.streams.StoreStream;
import me.wayne.daos.streams.StreamEntry;
import me.wayne.daos.streams.StreamId;

public class XReadCommand extends AbstractCommand<Map<String, SortedSet<StreamEntry>>> {

    XRangeCommand xRange = new XRangeCommand();

    public XReadCommand() {
        super("XREAD", 3, false);
    }

    @SuppressWarnings("all")
    @Override
    protected Map<String, SortedSet<StreamEntry>> processCommand(StorePrintWriter out, InMemoryStore store, List<String> args) {

        Map<String, Object> parsedCommand = parseXReadCommand(args);
        List<String> keys = (List<String>) parsedCommand.get("keys");
        List<String> ids = (List<String>) parsedCommand.get("ids");
        int count = (int) parsedCommand.get("count");
        long blockTimeout = (long) parsedCommand.get("block");

        LinkedHashMap<String, SortedSet<StreamEntry>> streams = new LinkedHashMap<>();
        for (int i = 0;i < keys.size();i++) {
            String key = keys.get(i);
            String id = ids.get(i);

            StoreStream storeStream = store.getStoreValue(key, StoreStream.class, true);

            if (id.equals("$")) id = storeStream.isEmpty() ? "0-0" : storeStream.getLastEntry().getId().toString();

            if (blockTimeout <= 0) {
                streams.put(key, storeStream.read(out, requestUuid, id, count, null));
            } else {
                storeStream.read(out, requestUuid, id, count, blockTimeout);
            }
        }

        if (blockTimeout <= 0) out.println(requestUuid, streams);
        return null;
    }

    public Map<String, Object> parseXReadCommand(List<String> tokens) {
        Map<String, Object> result = new HashMap<>();
        List<String> keys = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        int count = 0;
        long block = 0;

        int index = 0; // Start after the "XREAD" command
        while (index < tokens.size()) {
            String token = tokens.get(index);
            switch (token.toUpperCase()) {
                case "COUNT":
                    count = Integer.parseInt(tokens.get(++index));
                    break;
                case "BLOCK":
                    block = Long.parseLong(tokens.get(++index));
                    break;
                case "STREAMS":
                    index++;
                    while (index < tokens.size() && !StreamId.isValidId(tokens.get(index)) && !tokens.get(index).equals("$")) {
                        keys.add(tokens.get(index++));
                    }
                    while (index < tokens.size()) {
                        ids.add(tokens.get(index++));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected token: " + token);
            }
            index++;
        }

        result.put("count", count);
        result.put("block", block);
        result.put("keys", keys);
        result.put("ids", ids);

        return result;
    }
    
}
