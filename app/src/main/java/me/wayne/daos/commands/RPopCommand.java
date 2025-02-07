package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.List;

import me.wayne.AssertUtil;
import me.wayne.InMemoryStore;

public class RPopCommand extends AbstractCommand<List<String>> {

    public RPopCommand() {
        super("RPOP", 1, 2);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<String> processCommand(InMemoryStore store, List<String> args) {
        String key = args.get(0);
        int count = args.size() > 1 ? Integer.parseInt(args.get(1)) : 1;
        AssertUtil.assertTrue(store.getStore().containsKey(key), KEY_DOESNT_EXIST_MSG);
        AssertUtil.assertTrue(store.getStore().get(key) instanceof List, NON_LIST_ERROR_MSG);
        ArrayList<String> list = new ArrayList<>((List<String>) store.getStore().get(key));
        ArrayList<String> removeds = new ArrayList<>();
        for (int i = 0;i < count;i++) removeds.add(list.removeLast());
        store.getStore().put(key, list);
        return removeds;
    }
    
}
