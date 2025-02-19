package me.wayne.daos.storevalues;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class StoreSortedSet extends TreeSet<ScoreMember> {

    public int add(boolean nx, boolean xx, boolean gt, boolean lt, boolean ch, boolean incr, ScoreMember scoreMember) {
        if (contains(new ScoreMember(scoreMember.getMember()))) {
            ScoreMember existingScoreMember = floor(new ScoreMember(scoreMember.getMember()));
            if (nx) return 0;
            if (gt && scoreMember.getScore() <= existingScoreMember.getScore()) return 0;
            if (lt && scoreMember.getScore() >= existingScoreMember.getScore()) return 0;

            if (incr) {
                scoreMember = new ScoreMember(scoreMember.getScore() + existingScoreMember.getScore(), scoreMember.getMember());
            } else if (scoreMember.getScore().equals(existingScoreMember.getScore())) return 0;

            remove(existingScoreMember);
            add(scoreMember);
            return ch ? 1 : 0;
        } else {
            if (xx) return 0;
            add(scoreMember);
            return incr ? scoreMember.getScore() : 1;
        }
    }

    public List<String> range(int start, int stop, boolean withScores, boolean rev,
            boolean byScore) {
        if (start < 0) start += size();
        if (stop < 0) stop += size();
        PrintableList<String> range = new PrintableList<>();
        if (!byScore) {
            int i = 0;
            for (ScoreMember scoreMember : this) {
                if (i >= start && i <= stop) {
                    range.add(scoreMember.getMember());
                    if (withScores) range.add(scoreMember.getScore().toString());
                }
                i++;
            }
            return !rev ? range : range.reversed();
        } else {
            List<ScoreMember> scoreMembers = new ArrayList<>(this);
            for (int j = 0; j < scoreMembers.size(); j++) {
                if (scoreMembers.get(j).getScore() >= start && scoreMembers.get(j).getScore() <= stop) {
                    range.add(scoreMembers.get(j).getMember());
                    if (withScores) range.add(scoreMembers.get(j).getScore().toString());
                }
            }
            return !rev ? range : range.reversed();
        }
    }
    
}
