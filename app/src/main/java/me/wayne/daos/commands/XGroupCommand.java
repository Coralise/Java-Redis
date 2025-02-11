package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import me.wayne.AssertUtil;
import me.wayne.InMemoryStore;
import me.wayne.daos.StoreValue;
import me.wayne.daos.streams.ConsumerGroup;
import me.wayne.daos.streams.StoreStream;
import me.wayne.daos.streams.StreamEntry;
import me.wayne.daos.streams.StreamId;

public class XGroupCommand extends AbstractCommand<String> {

    public XGroupCommand() {
        super("XGROUP", 4, 7);
    }

    @Override
    protected String processCommand(PrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(1);
        String group = args.get(2);
        String id = args.get(3);
        boolean mkStream = args.contains("MKSTREAM");

        StoreValue storeValue = store.getStoreValue(key);
        if (storeValue == null) {
            if (!mkStream) return "ERR Stream does not exist";
            storeValue = new StoreValue(new StoreStream());
            store.setStoreValue(key, storeValue.getValue());
        }
        StoreStream stream = storeValue.getValue(StoreStream.class);

        return stream.createConsumerGroup(group, id);
    }
}
