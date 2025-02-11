package me.wayne.daos;

public class ScoreMember implements Comparable<ScoreMember> {
    
    private final Integer score;
    private final String member;

    public ScoreMember(Integer score, String member) {
        this.score = score;
        this.member = member;
    }

    public ScoreMember(String member) {
        this(null, member);
    }

    public Integer getScore() {
        return score;
    }

    public String getMember() {
        return member;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((score == null) ? 0 : score.hashCode());
        result = prime * result + ((member == null) ? 0 : member.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScoreMember other = (ScoreMember) obj;
        if (score == null) {
            if (other.score != null)
                return false;
        } else if (!score.equals(other.score))
            return false;
        if (member == null) {
            if (other.member != null)
                return false;
        } else if (!member.equals(other.member))
            return false;
        return true;
    }

    @Override
    public int compareTo(ScoreMember o) {
        if (o.score != null && score != null && score.compareTo(o.score) != 0) return score.compareTo(o.score);
        return member.compareTo(o.member);
    }

}
