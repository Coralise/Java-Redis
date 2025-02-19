package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.PrintableList;
import me.wayne.daos.storevalues.streams.StoreStream;
import me.wayne.daos.storevalues.streams.StreamEntry;

public class XRangeCommand extends AbstractCommand<PrintableList<StreamEntry>> {

    public XRangeCommand() {
        super("XRANGE", 3);
    }

    @SuppressWarnings("all")
    @Override
    protected PrintableList<StreamEntry> processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        String start = args.get(1);
        String end = args.get(2);
        int count = args.size() > 3 ? Integer.parseInt(args.get(4)) : -1;
        StoreStream streamList = store.getStoreValue(key, StoreStream.class, new StoreStream(key));

        boolean startInclusive = true;
        boolean endInclusive = true;    

        if (start.startsWith("(")) {
            startInclusive = false;
            start = start.substring(1);
        }
        if (end.startsWith("(")) {
            endInclusive = false;
            end = end.substring(1);
        }

        return new PrintableList<>(streamList.range(
            start,
            startInclusive,
            end,
            endInclusive,
            count).getEntries());
    }
    
}
