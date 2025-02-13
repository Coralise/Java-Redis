package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.wayne.AssertUtil;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.ScoreMember;
import me.wayne.daos.storevalues.StoreSortedSet;
import me.wayne.daos.storevalues.StoreValue;

public class ZAddCommand extends AbstractCommand<Integer> {

    public ZAddCommand() {
        super("ZADD", 3);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        ZAddArguments zAddArguments = parseZAddArguments(args.subList(1, args.size()));

        StoreValue storeValue = store.getStoreValue(key);
        StoreSortedSet treeSet = storeValue != null ? storeValue.getValue(StoreSortedSet.class) : new StoreSortedSet();

        boolean nx = zAddArguments.options.contains("NX");
        boolean xx = zAddArguments.options.contains("XX");
        AssertUtil.assertTrue(!(nx && xx), "Both NX and XX options are specified");
        boolean gt = zAddArguments.options.contains("GT");
        boolean lt = zAddArguments.options.contains("LT");
        AssertUtil.assertTrue(!(gt && lt), "Both GT and LT options are specified");
        boolean ch = zAddArguments.options.contains("CH");
        boolean incr = zAddArguments.options.contains("INCR");

        List<String> scoresAndMembers = zAddArguments.scoresAndMembers;

        if (incr) AssertUtil.assertTrue(scoresAndMembers.size() == 2, "INCR option requires a single score and member");

        int updated = 0;

        for (int i = 1; i < scoresAndMembers.size(); i += 2) {
            int score = parseScore(scoresAndMembers.get(i-1));
            ScoreMember scoreMember = new ScoreMember(score, scoresAndMembers.get(i));
            updated += processScoreMember(treeSet, nx, xx, gt, lt, ch, incr, scoreMember);
        }

        store.setStoreValue(key, treeSet, inputLine);

        return updated;
    }

    private int processScoreMember(StoreSortedSet treeSet, boolean nx, boolean xx, boolean gt, boolean lt, boolean ch, boolean incr, ScoreMember scoreMember) {
        if (treeSet.contains(new ScoreMember(scoreMember.getMember()))) {
            ScoreMember existingScoreMember = treeSet.floor(new ScoreMember(scoreMember.getMember()));
            if (nx) return 0;
            if (gt && scoreMember.getScore() <= existingScoreMember.getScore()) return 0;
            if (lt && scoreMember.getScore() >= existingScoreMember.getScore()) return 0;

            if (incr) scoreMember = new ScoreMember(scoreMember.getScore() + existingScoreMember.getScore(), scoreMember.getMember());
            else if (scoreMember.getScore().equals(existingScoreMember.getScore())) return 0;

            treeSet.remove(existingScoreMember);
            treeSet.add(scoreMember);
            return ch ? 1 : 0;
        } else {
            if (xx) return 0;
            treeSet.add(scoreMember);
            return 1;
        }
    }

    private int parseScore(String scoreStr) {
        try {
            return Integer.parseInt(scoreStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid score: " + scoreStr, e);
        }
    }

    private ZAddArguments parseZAddArguments(List<String> args) {
        Set<String> options = new HashSet<>();
        List<String> scoresAndMembers = new ArrayList<>();

        int i = 0;
        while (i < args.size()) {
            String arg = args.get(i).toUpperCase();
            if (arg.equals("NX") || arg.equals("XX") || arg.equals("GT") || arg.equals("LT") || arg.equals("CH") || arg.equals("INCR")) {
                options.add(arg);
                i++;
            } else {
                break;
            }
        }

        while (i < args.size()) {
            scoresAndMembers.add(args.get(i));
            i++;
        }

        return new ZAddArguments(options, scoresAndMembers);
    }

    private static class ZAddArguments {
        Set<String> options;
        List<String> scoresAndMembers;

        ZAddArguments(Set<String> options, List<String> scoresAndMembers) {
            this.options = options;
            this.scoresAndMembers = scoresAndMembers;
        }
    }
    
}
