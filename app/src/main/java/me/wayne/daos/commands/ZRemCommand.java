package me.wayne.daos.commands;

import java.util.List;
import java.util.TreeSet;

import me.wayne.InMemoryStore;
import me.wayne.daos.ScoreMember;

public class ZRemCommand extends AbstractCommand<Integer> {

    public ZRemCommand() {
        super("ZREM", 2);
    }

    @Override
    protected Integer processCommand(InMemoryStore store, List<String> args) {
        String key = args.get(0);
        List<String> members = args.subList(1, args.size());
        TreeSet<ScoreMember> treeSet = getTreeSet(store, key);
        int removed = 0;
        for (String member : members) {
            ScoreMember scoreMember = getScoreMember(store, key, member);
            if (scoreMember != null && treeSet.remove(scoreMember)) {
                removeFromTreeSetMembers(store, key, scoreMember);
                removed++;
            }
            
        }
        store.getStore().put(key, treeSet);
        return removed;
    }
    
}
