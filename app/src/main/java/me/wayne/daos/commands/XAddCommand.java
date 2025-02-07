package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import me.wayne.InMemoryStore;
import me.wayne.daos.ConsumerGroup;
import me.wayne.daos.StreamEntry;
import me.wayne.daos.StreamId;

public class XAddCommand extends AbstractCommand<String> {

    public XAddCommand() {
        super("XADD", 4);
    }

    @SuppressWarnings("all")
    @Override
    protected String processCommand(Thread thread, InMemoryStore store, List<String> args) {

        String key = args.get(0);
        XAddArguments xAddArguments = parseXAddArguments(args);
        String id = xAddArguments.id;
        List<String> fieldsAndValues = xAddArguments.fieldsAndValues;

        Pattern pattern = Pattern.compile("\\d+-\\d+");
        if (id.equals("*")) {
            id = System.currentTimeMillis() + "-";
        } else if (pattern.matcher(id).matches()) {
            // Continue
        } else if (id.matches("\\d+")) {
            id += "-";
        } else {
            throw new IllegalArgumentException("Invalid ID format");
        }

        ArrayList<StreamEntry> streamList = getStreamList(store, key);

        int sequence = pattern.matcher(id).matches() ? Integer.parseInt(id.split("-")[1]) : 0;
        while (streamList.contains(id + sequence)) sequence++;
        id += sequence;

        ArrayList<String> fieldsAndValuesList = new ArrayList<>();
        for (int i = 1; i < fieldsAndValues.size(); i += 2) {
            fieldsAndValuesList.add(fieldsAndValues.get(i-1));
            fieldsAndValuesList.add(fieldsAndValues.get(i));
        }

        StreamEntry streamEntry = new StreamEntry(new StreamId(id), fieldsAndValuesList);
        streamList.add(streamEntry);
        store.getStore().put(key, streamList);

        ConsumerGroup consumerGroup = store.getConsumerGroupByStreamKey(key);
        if (consumerGroup != null && consumerGroup.hasConsumers()) {
            consumerGroup.feedEntry(streamEntry);
        }

        return id;
    }

    private XAddArguments parseXAddArguments(List<String> args) {
        Set<String> options = new HashSet<>();
        String id = null;
        List<String> fieldsAndValues = new ArrayList<>();
        String threshold = null;
        String thresholdType = null;
        int limitCount = -1;

        int i = 1;
        while (i < args.size()) {
            String arg = args.get(i).toUpperCase();
            if (arg.equals("NOMKSTREAM")) {
                options.add(arg);
                i++;
            } else if (arg.equals("MAXLEN") || arg.equals("MINID")) {
                thresholdType = arg;
                i++;
                if (args.get(i).equals("=") || args.get(i).equals("~")) {
                    threshold = args.get(i + 1);
                    i += 2;
                } else {
                    threshold = args.get(i);
                    i++;
                }
                if (i < args.size() && args.get(i).equals("LIMIT")) {
                    limitCount = Integer.parseInt(args.get(i + 1));
                    i += 2;
                }
            } else {
                id = args.get(i);
                i++;
                break;
            }
        }

        while (i < args.size()) {
            fieldsAndValues.add(args.get(i));
            i++;
        }

        return new XAddArguments(options, id, fieldsAndValues, thresholdType, threshold, limitCount);
    }

    private static class XAddArguments {
        Set<String> options;
        String id;
        List<String> fieldsAndValues;
        String thresholdType;
        String threshold;
        int limitCount;

        XAddArguments(Set<String> options, String id, List<String> fieldsAndValues, String thresholdType, String threshold, int limitCount) {
            this.options = options;
            this.id = id;
            this.fieldsAndValues = fieldsAndValues;
            this.thresholdType = thresholdType;
            this.threshold = threshold;
            this.limitCount = limitCount;
        }
    }
    
}
