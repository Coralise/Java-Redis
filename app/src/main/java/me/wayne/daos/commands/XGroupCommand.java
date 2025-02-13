package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.StoreValue;
import me.wayne.daos.storevalues.streams.StoreStream;

public class XGroupCommand extends AbstractCommand<String> {

    public XGroupCommand() {
        super("XGROUP", 4, 7);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(1);
        String group = args.get(2);
        String id = args.get(3);
        boolean mkStream = args.contains("MKSTREAM");

        StoreValue storeValue = store.getStoreValue(key);
        if (storeValue == null) {
            if (!mkStream) return "ERR Stream does not exist";
            storeValue = new StoreValue(new StoreStream());
            store.setStoreValue(key, storeValue.getValue(), inputLine);
        }
        StoreStream stream = storeValue.getValue(StoreStream.class);

        return stream.createConsumerGroup(group, id);
    }
}
