package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.List;

import me.wayne.AssertUtil;
import me.wayne.InMemoryStore;

public class LRangeCommand extends AbstractCommand<List<String>> {

    public LRangeCommand() {
        super("LRANGE", 3, 3);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<String> processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        int start = Integer.parseInt(args.get(1));
        int stop = Integer.parseInt(args.get(2));
        AssertUtil.assertTrue(store.getStore().containsKey(key), KEY_DOESNT_EXIST_MSG);
        AssertUtil.assertTrue(store.getStore().get(key) instanceof List, NON_LIST_ERROR_MSG);
        ArrayList<String> list = new ArrayList<>((List<String>) store.getStore().get(key));
        ArrayList<String> range = new ArrayList<>();
        while (stop < 0) stop += list.size();
        for (int i = Math.max(start, 0);i <= stop && i < list.size();i++) range.add(list.get(i));
        return range;
    }
    
}
