package me.wayne.daos.commands;

import java.util.List;
import java.util.TreeSet;

import me.wayne.InMemoryStore;
import me.wayne.daos.ScoreMember;

public class ZRankCommand extends AbstractCommand<Object> {

    public ZRankCommand() {
        super("ZRANK", 2, 3);
    }

    @Override
    protected Object processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        String member = args.get(1);
        boolean withScore = args.size() == 3 && args.get(2).equals("WITHSCORE");
        TreeSet<ScoreMember> treeSet = getTreeSet(store, key);
        ScoreMember scoreMember = getScoreMember(store, key, member);
        if (scoreMember == null) return null;
        int rank = treeSet.headSet(scoreMember).size();
        return withScore ? List.of(rank, scoreMember.getScore()) : rank;
    }
    
}
