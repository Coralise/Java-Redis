package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.ScoreMember;
import me.wayne.daos.StoreSortedSet;
import me.wayne.daos.StoreValue;
import me.wayne.daos.io.StorePrintWriter;

public class ZRemCommand extends AbstractCommand<Integer> {

    public ZRemCommand() {
        super("ZREM", 2);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        List<String> members = args.subList(1, args.size());
        StoreValue storeValue = store.getStoreValue(key);
        StoreSortedSet treeSet = storeValue != null ? storeValue.getValue(StoreSortedSet.class) : new StoreSortedSet();
        int removed = 0;
        for (String member : members) {
            ScoreMember scoreMember = treeSet.floor(new ScoreMember(member));
            if (scoreMember != null && scoreMember.getMember().equals(member) && treeSet.remove(scoreMember)) {
                removed++;
            }
            
        }
        store.setStoreValue(key, treeSet);
        return removed;
    }
    
}
