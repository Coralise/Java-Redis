package me.wayne.daos.commands;

import java.util.List;

import me.wayne.AssertUtil;
import me.wayne.InMemoryStore;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.streams.ConsumerGroup;
import me.wayne.daos.streams.StreamId;

public class XAckCommand extends AbstractCommand<Integer> {

    public XAckCommand() {
        super("XACK", 3);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, InMemoryStore store, List<String> args) {
        
        String key = args.get(0);
        String group = args.get(1);
        List<String> ids = args.subList(2, args.size());

        // ConsumerGroup consumerGroup = store.getConsumerGroupByGroupName(group);
        // if (consumerGroup == null) throw new AssertionError("ERROR: Consumer group not found");
        // AssertUtil.assertTrue(consumerGroup.getStreamKey().equals(key), "ERROR: Invalid stream key");

        // int acknowledged = 0;
        // for (String id : ids) {
        //     if ((StreamId.isValidId(id) || StreamId.isValidPartialId(id)) && consumerGroup.acknowledgeEntry(new StreamId(id))) acknowledged++;
        // }

        return -1;

    }
    
}
