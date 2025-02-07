package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.List;

import me.wayne.AssertUtil;
import me.wayne.InMemoryStore;

public class LIndexCommand extends AbstractCommand<String> {

    public LIndexCommand() {
        super("LINDEX", 2, 2);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected String processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        int index = Integer.parseInt(args.get(1));
        AssertUtil.assertTrue(store.getStore().containsKey(key), KEY_DOESNT_EXIST_MSG);
        AssertUtil.assertTrue(store.getStore().get(key) instanceof List, NON_LIST_ERROR_MSG);
        ArrayList<String> list = new ArrayList<>((List<String>) store.getStore().get(key));
        while (index < 0) index += list.size();
        return index < list.size() ? list.get(index) : null;
    }
    
}
