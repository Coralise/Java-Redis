package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import me.wayne.InMemoryStore;
import me.wayne.daos.ScoreMember;

public class ZRangeCommand extends AbstractCommand<List<String>> {

    public ZRangeCommand() {
        super("ZRANGE", 3);
    }

    @Override
    protected List<String> processCommand(InMemoryStore store, List<String> args) {
        String key = args.get(0);
        int start = Integer.parseInt(args.get(1));
        int stop = Integer.parseInt(args.get(2));

        ZRangeArguments zRangeArguments = parseZRangeArguments(args);
        Set<String> options = zRangeArguments.options;

        TreeSet<ScoreMember> treeSet = getTreeSet(store, key);
        List<String> range = new ArrayList<>();
        int i = 0;
        while (start < 0) start += treeSet.size();
        while (stop < 0) stop += treeSet.size();

        boolean withScores = options.contains("WITHSCORES");
        boolean rev = options.contains("REV");
        boolean byScore = options.contains("BYSCORE");

        if (!byScore) {
            for (ScoreMember scoreMember : treeSet) {
                if (i >= start && i <= stop) {
                    range.add(scoreMember.getMember());
                    if (withScores) range.add(scoreMember.getScore().toString());
                }
                i++;
            }
            return !rev ? range : range.reversed();
        } else {
            List<ScoreMember> scoreMembers = new ArrayList<>(treeSet);
            for (int j = 0; j < scoreMembers.size(); j++) {
                if (scoreMembers.get(j).getScore() >= start && scoreMembers.get(j).getScore() <= stop) {
                    range.add(scoreMembers.get(j).getMember());
                    if (withScores) range.add(scoreMembers.get(j).getScore().toString());
                }
            }
            return !rev ? range : range.reversed();
        }
    }

    private ZRangeArguments parseZRangeArguments(List<String> args) {
        Set<String> options = new HashSet<>();
        int offset = 0;
        int count = 0;

        int i = 3;
        while (i < args.size()) {
            String arg = args.get(i).toUpperCase();
            if (arg.equals("BYSCORE") || arg.equals("BYLEX") || arg.equals("REV") || arg.equals("WITHSCORES")) {
                options.add(arg);
                i++;
            } else if (arg.equals("LIMIT")) {
                options.add(arg);
                offset = Integer.parseInt(args.get(i + 1));
                count = Integer.parseInt(args.get(i + 2));
                i += 3;
            } else {
                break;
            }
        }

        return new ZRangeArguments(options, offset, count);
    }

    private static class ZRangeArguments {
        Set<String> options;
        int offset;
        int count;

        ZRangeArguments(Set<String> options, int offset, int count) {
            this.options = options;
            this.offset = offset;
            this.count = count;
        }
    }
    
}
