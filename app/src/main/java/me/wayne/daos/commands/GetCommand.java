package me.wayne.daos.commands;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Level;

import me.wayne.InMemoryStore;

public class GetCommand extends AbstractCommand<Object> {

    public GetCommand() {
        super("GET", 1, 1);
    }

    @Override
    protected Object processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        Object object = store.getStore().get(key);

        if (object instanceof byte[] bytes) {
            object = new String(bytes);
        }

        return object;
    }

}