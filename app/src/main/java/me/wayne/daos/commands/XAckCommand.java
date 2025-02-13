package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.streams.ConsumerGroup;
import me.wayne.daos.storevalues.streams.StoreStream;
import me.wayne.daos.storevalues.streams.StreamId;

public class XAckCommand extends AbstractCommand<Integer> {

    public XAckCommand() {
        super("XACK", 3);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        
        String key = args.get(0);
        String group = args.get(1);
        List<String> ids = args.subList(2, args.size());

        StoreStream stream = store.getStoreValue(key, StoreStream.class, false);
        
        ConsumerGroup consumerGroup = stream.getConsumerGroupByGroupName(group);
        if (consumerGroup == null) {
            return 0;
        }

        int count = 0;
        for (String id : ids) {
            count += consumerGroup.acknowledgeEntry(new StreamId(id)) ? 1 : 0;
        }

        return count;

    }
    
}
