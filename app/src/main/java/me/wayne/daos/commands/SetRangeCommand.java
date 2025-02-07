package me.wayne.daos.commands;

import java.util.List;

import me.wayne.AssertUtil;
import me.wayne.InMemoryStore;

public class SetRangeCommand extends AbstractCommand<String> {

    public SetRangeCommand() {
        super("SETRANGE", 3, 3);
    }

    @Override
    protected String processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        int offset = Integer.parseInt(args.get(1));
        String value = args.get(2);
        AssertUtil.assertTrue(store.getStore().containsKey(key), KEY_DOESNT_EXIST_MSG);
        AssertUtil.assertTrue(store.getStore().get(key) instanceof String, NON_STRING_ERROR_MSG);
        String existingValue = (String) store.getStore().get(key);
        StringBuilder newValue = new StringBuilder(existingValue);
        newValue.replace(offset, offset + value.length(), value);
        store.getStore().put(key, newValue.toString());
        return newValue.toString();
    }
    
}
