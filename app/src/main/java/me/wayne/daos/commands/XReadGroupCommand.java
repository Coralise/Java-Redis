package me.wayne.daos.commands;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.wayne.AssertUtil;
import me.wayne.InMemoryStore;
import me.wayne.daos.ConsumerGroup;
import me.wayne.daos.StreamEntry;
import me.wayne.daos.StreamId;

public class XReadGroupCommand extends AbstractCommand<Map<String, List<StreamEntry>>> {

    public XReadGroupCommand() {
        super("XREADGROUP", 5);
    }

    @Override
    protected Map<String, List<StreamEntry>> processCommand(Thread thread, InMemoryStore store, List<String> args) {

        Map<String, Object> extractedArgs = extractArgsAndOptions(args);
        String group = (String) extractedArgs.get("group");
        String consumer = (String) extractedArgs.get("consumer");
        int count = (int) extractedArgs.get("count");
        long block = (long) extractedArgs.get("block");
        boolean noAck = (boolean) extractedArgs.get("noAck");
        String key = (String) extractedArgs.get("key");
        String id = (String) extractedArgs.get("id");

        ConsumerGroup consumerGroup = store.getConsumerGroupByGroupName(group);
        if (consumerGroup == null) throw new AssertionError("ERROR: Consumer group does not exist");
        AssertUtil.assertTrue(consumerGroup.getStreamKey().equals(key), "ERROR: Key does not match: " + key + " != " + consumerGroup.getStreamKey());

        StreamId streamId;
        if (id.equals(">")) streamId = consumerGroup.getLastDeliveredEntryId();
        else if (StreamId.isValidId(id)) streamId = new StreamId(id);
        else if (StreamId.isValidPartialId(id)) streamId = new StreamId(id + "-0");
        else throw new IllegalArgumentException("Invalid ID");
        
        Map<String, List<StreamEntry>> result = new XReadCommand().executeCommand(thread, store, "XREAD " + (count > 0 ? "COUNT " + count + " " : "") + "STREAMS " + key + " " + streamId);
        if (!result.get(key).isEmpty() || !id.equals(">") || block <= 0) {
            List<StreamEntry> entries = result.get(key);
            if (entries != null && !entries.isEmpty()) {
                StreamEntry lastEntry = entries.get(entries.size() - 1);
                consumerGroup.setLastDeliveredEntryId(lastEntry.getId());
            }
            return result;
        } else {
            result = null;

            AssertUtil.assertTrue(!consumerGroup.hasConsumer(consumer), "ERROR: Consumer already exists");

            consumerGroup.addConsumer(consumer, thread, noAck);

            try {
                Thread.sleep(block);
            } catch (InterruptedException e) {
                result = new XReadCommand().executeCommand(thread, store, "XREAD " + (count > 0 ? "COUNT " + count + " " : "") + "STREAMS " + key + " " + streamId);
            }
            consumerGroup.removeConsumer(consumer);
        }

        return result;
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
