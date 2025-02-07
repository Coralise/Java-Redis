package me.wayne.daos.commands;

import java.util.List;

import me.wayne.AssertUtil;
import me.wayne.InMemoryStore;

public class GetRangeCommand extends AbstractCommand<String> {

    public GetRangeCommand() {
        super("GETRANGE", 3, 3);
    }

    @Override
    protected String processCommand(InMemoryStore store, List<String> args) {
        String key = args.get(0);
        int start = Integer.parseInt(args.get(1));
        int end = Integer.parseInt(args.get(2));
        
        AssertUtil.assertTrue(store.getStore().containsKey(key), KEY_DOESNT_EXIST_MSG);
        AssertUtil.assertTrue(store.getStore().get(key) instanceof String, NON_STRING_ERROR_MSG);
        String value = (String) store.getStore().get(key);
        return value.substring(start, end + 1);
    }
    
}
