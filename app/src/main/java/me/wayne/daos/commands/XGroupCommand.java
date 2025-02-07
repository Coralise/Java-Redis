package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.List;

import me.wayne.AssertUtil;
import me.wayne.InMemoryStore;
import me.wayne.daos.ConsumerGroup;
import me.wayne.daos.StreamEntry;
import me.wayne.daos.StreamId;

public class XGroupCommand extends AbstractCommand<String> {

    public XGroupCommand() {
        super("XGROUP", 4, 7);
    }

    @Override
    protected String processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(1);
        String group = args.get(2);
        String id = args.get(3);
        boolean mkStream = args.contains("MKSTREAM");

        if (!store.getStore().containsKey(key)) {
            if (!mkStream) return "ERROR: Stream does not exist";
            store.getStore().put(key, new ArrayList<StreamEntry>());
        }
        
        AssertUtil.assertTrue(store.getStore().get(key) instanceof List list && list.get(0) instanceof StreamEntry, "ERROR: Value is not a stream");
        AssertUtil.assertTrue(!store.hasCustomerGroup(group), "ERROR: Consumer group already exists");

        if (id.equals("$")) {
            ArrayList<StreamEntry> streamEntries = new XRangeCommand().executeCommand(thread, store, "XRANGE " + key + " - + COUNT 1");
            if (streamEntries.isEmpty()) {
                id = "0";
            } else {
                id = streamEntries.get(0).getId().getTimeStamp().toString();
            }
        } else if (!StreamId.isValidId(id)) {
            return "ERROR: Invalid ID";
        }

        ConsumerGroup consumerGroup = new ConsumerGroup(group, key, new StreamId(id));
        store.addConsumerGroup(consumerGroup);

        return OK_RESPONSE;
    }
}
