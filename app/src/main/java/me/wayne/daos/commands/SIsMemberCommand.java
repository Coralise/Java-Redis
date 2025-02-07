package me.wayne.daos.commands;

import java.util.HashSet;
import java.util.List;

import me.wayne.InMemoryStore;

public class SIsMemberCommand extends AbstractCommand<Integer> {

    public SIsMemberCommand() {
        super("SISMEMBER", 2, 2);
    }

    @Override
    protected Integer processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        String value = args.get(1);
        if (!store.getStore().containsKey(key)) return 0;
        HashSet<String> hashSet = getHashSet(store, key);
        return hashSet.contains(value) ? 1 : 0;
    }
    
}
