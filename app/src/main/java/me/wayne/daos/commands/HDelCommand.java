package me.wayne.daos.commands;

import java.util.List;
import java.util.Map;

import me.wayne.InMemoryStore;

public class HDelCommand extends AbstractCommand<Integer> {

    public HDelCommand() {
        super("HDEL", 2);
    }

    @Override
    protected Integer processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        List<String> fields = args.subList(1, args.size());
        if (!store.getStore().containsKey(key)) return 0;
        Map<String, String> hashMap = getMap(store, key);
        int removed = 0;
        for (String field : fields) if (hashMap.remove(field) != null) removed++;
        store.getStore().put(key, hashMap);
        return removed;
    }
    
}
