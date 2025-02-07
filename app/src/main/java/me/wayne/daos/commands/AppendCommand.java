package me.wayne.daos.commands;

import java.util.List;

import me.wayne.AssertUtil;
import me.wayne.InMemoryStore;

public class AppendCommand extends AbstractCommand<String> {

    public AppendCommand() {
        super("APPEND", 2, 2);
    }

    @Override
    protected String processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        String value = args.get(1);
        AssertUtil.assertTrue(store.getStore().containsKey(key), KEY_DOESNT_EXIST_MSG);
        AssertUtil.assertTrue(store.getStore().get(key) instanceof CharSequence, "Existing value is not of type CharSequence");
        AssertUtil.assertTrue(value instanceof CharSequence, "Value to append is not of type CharSequence");
        store.getStore().put(key, store.getStore().get(key) + value);
        return OK_RESPONSE;
    }
    
}
