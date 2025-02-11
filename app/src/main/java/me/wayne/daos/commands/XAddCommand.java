package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import me.wayne.InMemoryStore;
import me.wayne.daos.streams.StoreStream;
import me.wayne.daos.streams.StreamId;

public class XAddCommand extends AbstractCommand<String> {

    public XAddCommand() {
        super("XADD", 4);
    }

    @SuppressWarnings("all")
    @Override
    protected String processCommand(PrintWriter out, InMemoryStore store, List<String> args) {

        String key = args.get(0);
        XAddArguments xAddArguments = parseXAddArguments(args);
        String id = StreamId.parseStreamId(xAddArguments.id);
        List<String> fieldsAndValues = xAddArguments.fieldsAndValues;

        StoreStream streamList = store.getStoreValue(key, StoreStream.class, new StoreStream());

        String res = streamList.add(id, fieldsAndValues);
        store.setStoreValue(key, streamList);
        return res;
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
