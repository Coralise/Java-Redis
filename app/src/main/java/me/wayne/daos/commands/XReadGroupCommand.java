package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.streams.StoreStream;

public class XReadGroupCommand extends AbstractCommand<String> {

    public XReadGroupCommand() {
        super("XREADGROUP", 5, false);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {

        Map<String, Object> extractedArgs = extractArgsAndOptions(args);
        String group = (String) extractedArgs.get("group");
        String consumer = (String) extractedArgs.get("consumer");
        int count = (int) extractedArgs.get("count");
        long block = (long) extractedArgs.get("block");
        boolean noAck = (boolean) extractedArgs.get("noAck");
        String key = (String) extractedArgs.get("key");
        String id = (String) extractedArgs.get("id");

        StoreStream stream = store.getStoreValue(key, StoreStream.class, true);

        stream.readGroup(
            out,
            group,
            consumer,
            count,
            block <= 0 ? null : block,
            noAck,
            id);

        return OK_RESPONSE;

    }

    private Map<String, Object> extractArgsAndOptions(List<String> args) {
        String group = args.get(1);
        String consumer = args.get(2);
        int count = 0;
        long block = 0;
        boolean noAck = false;
        String key = null;
        String id = null;

        int index = 3;
        while (index < args.size()) {
            String arg = args.get(index);
            switch (arg.toUpperCase()) {
                case "COUNT":
                    count = Integer.parseInt(args.get(++index));
                    break;
                case "BLOCK":
                    block = Long.parseLong(args.get(++index));
                    break;
                case "NOACK":
                    noAck = true;
                    break;
                case "STREAMS":
                    index++;
                    if (index < args.size()) {
                        key = args.get(index++);
                    }
                    if (index < args.size()) {
                        id = args.get(index++);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument: " + arg);
            }
            index++;
        }

        Map<String, Object> extractedArgs = new LinkedHashMap<>();
        extractedArgs.put("group", group);
        extractedArgs.put("consumer", consumer);
        extractedArgs.put("count", count);
        extractedArgs.put("block", block);
        extractedArgs.put("noAck", noAck);
        extractedArgs.put("key", key);
        extractedArgs.put("id", id);

        return extractedArgs;
    }
    
}
