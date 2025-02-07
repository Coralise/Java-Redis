package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.StreamEntry;

public class XRangeCommand extends AbstractCommand<ArrayList<StreamEntry>> {

    public XRangeCommand() {
        super("XRANGE", 3);
    }

    @SuppressWarnings("all")
    @Override
    protected ArrayList<StreamEntry> processCommand(InMemoryStore store, List<String> args) {
        String key = args.get(0);
        String start = args.get(1);
        String end = args.get(2);
        int count = args.size() > 3 ? Integer.parseInt(args.get(4)) : -1;
        ArrayList<StreamEntry> streamList = getStreamList(store, key);
        ArrayList<StreamEntry> range = new ArrayList<>();

        boolean startExclusive = false;
        long startTimestamp = -1;
        int startSequence = -1;
        if (!start.equals("-")) {
            startExclusive = start.startsWith("(");
            startTimestamp = Long.parseLong(!startExclusive ? start.split("-")[0] : start.substring(1).split("-")[0]);
            startSequence = Integer.parseInt(start.split("-").length > 1 ? !start.split("-")[1].equals("") ? start.split("-")[1] : "0" : "0");
        }

        boolean endExclusive = false;        
        long endTimestamp = -1;        
        int endSequence = -1;            
        if (!end.equals("+")) {
            endExclusive = end.startsWith("(");
            endTimestamp = Long.parseLong(!endExclusive ? end.split("-")[0] : end.substring(1).split("-")[0]);
            endSequence = Integer.parseInt(end.split("-").length > 1 ? !end.split("-")[1].equals("") ? end.split("-")[1] : "0" : "0");
        }

        int i = 0;
        for (StreamEntry entry : streamList) {
            if (count > 0 && i >= count) break;
            long entryTimestamp = entry.getTimestamp();
            int entrySequence = entry.getSequence();
            boolean withinStartRange = start.equals("-") || (!startExclusive && (entryTimestamp > startTimestamp || (entryTimestamp == startTimestamp && entrySequence >= startSequence))) || (startExclusive && (entryTimestamp > startTimestamp || (entryTimestamp == startTimestamp && entrySequence > startSequence)));
            boolean withinEndRange = end.equals("+") || (!endExclusive && (entryTimestamp < endTimestamp || (entryTimestamp == endTimestamp && entrySequence <= endSequence))) || (endExclusive && (entryTimestamp < endTimestamp || (entryTimestamp == endTimestamp && entrySequence < endSequence)));

            if (withinStartRange && withinEndRange) {
                range.add(entry);
                i++;
            }
        }

        System.out.println(range);
        return range;
    }
    
}
