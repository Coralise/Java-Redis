package me.wayne.daos.commands;

import java.util.List;

import me.wayne.AssertUtil;
import me.wayne.InMemoryStore;

public class DecrCommand extends AbstractCommand<String> {

    public DecrCommand() {
        super("DECR", 1, 1);
    }

    @Override
    protected String processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        AssertUtil.assertTrue(store.getStore().containsKey(key), KEY_DOESNT_EXIST_MSG);
        int intValue = getValueAsInteger(store.getStore().get(key));
        store.getStore().put(key, intValue - 1);
        return OK_RESPONSE;
    }
    
}
