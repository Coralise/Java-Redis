package me.wayne.daos.commands;

import java.util.List;

import me.wayne.AssertUtil;
import me.wayne.InMemoryStore;

public class IncrByCommand extends AbstractCommand<String> {

    public IncrByCommand() {
        super("INCRBY", 2, 2);
    }

    @Override
    protected String processCommand(InMemoryStore store, List<String> args) {
        String key = args.get(0);
        int increment = getValueAsInteger(args.get(1));
        AssertUtil.assertTrue(store.getStore().containsKey(key), KEY_DOESNT_EXIST_MSG);
        int intValue = getValueAsInteger(store.getStore().get(key));
        store.getStore().put(key, intValue + increment);
        return OK_RESPONSE;
    }
    
}
