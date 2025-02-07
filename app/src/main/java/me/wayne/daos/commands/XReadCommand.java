package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.wayne.InMemoryStore;
import me.wayne.daos.StreamEntry;

public class XReadCommand extends AbstractCommand<Map<String, List<StreamEntry>>> {

    XRangeCommand xRange = new XRangeCommand();

    public XReadCommand() {
        super("XREAD", 3);
    }

    @SuppressWarnings("all")
    @Override
    protected Map<String, List<StreamEntry>> processCommand(InMemoryStore store, List<String> args) {
        LinkedHashMap<String, List<StreamEntry>> streams = new LinkedHashMap<>();

        Map<String, Object> parsedCommand = parseXReadCommand(args);
        List<String> keys = (List<String>) parsedCommand.get("keys");
        List<String> ids = (List<String>) parsedCommand.get("ids");
        int count = (int) parsedCommand.get("count");
        long blockTimeout = (long) parsedCommand.get("block");
        
        if (blockTimeout <= 0) {
            for (int i = 0;i < keys.size();i++) {
                String key = keys.get(i);
                String id = ids.get(i);
                List<StreamEntry> range = xRange.processCommand(store, List.of(key, "(" + id, "+", "COUNT", String.valueOf(count)));
                streams.put(key, range);
            }
        } else {
            long startTime = System.currentTimeMillis();
            boolean entriesFound = false;
            while (!entriesFound && (System.currentTimeMillis() - startTime) < blockTimeout) {
                for (int i = 0; i < keys.size(); i++) {
                    String key = keys.get(i);
                    String id = ids.get(i);
                    List<StreamEntry> range = xRange.processCommand(store, List.of(key, "(" + id, "+", "COUNT", String.valueOf(count)));
                    if (!range.isEmpty()) {
                        streams.put(key, range);
                        entriesFound = true;
                    }
                }
        
                if (!entriesFound) {
                    try {
                        Thread.sleep(100); 
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        return streams;
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
                    while (index < tokens.size() && !tokens.get(index).matches("\\d+-\\d+")) {
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
