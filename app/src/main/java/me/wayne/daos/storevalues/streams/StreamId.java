package me.wayne.daos.storevalues.streams;

import java.io.Serializable;

import javax.annotation.Nonnull;

import me.wayne.AssertUtil;

public class StreamId implements Comparable<StreamId>, Serializable {
    private static final long serialVersionUID = 1L;

    @Nonnull
    private final Long timeStamp;
    @Nonnull
    private final Integer sequence;

    public StreamId(Long timeStamp, Integer sequence) {
        this.timeStamp = timeStamp;
        this.sequence = sequence;
    }

    public StreamId(String id) {
        id = parseStreamId(id);
        this.timeStamp = Long.parseLong(id.split("-")[0]);
        this.sequence = Integer.parseInt(id.split("-")[1]);
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public Integer getSequence() {
        return sequence;
    }

    public static boolean isValidId(String id) {
        return id.matches("\\d+-(\\d+|\\*?)|\\d+|\\*|\\+|\\-");
    }

    @Override
    public String toString() {
        return timeStamp + "-" + sequence;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
        result = prime * result + ((sequence == null) ? 0 : sequence.hashCode());
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
        StreamId other = (StreamId) obj;
        if (!timeStamp.equals(other.timeStamp))
            return false;
        return sequence.equals(other.sequence);
    }

    @Override
    public int compareTo(StreamId o) {
        if (timeStamp.compareTo(o.timeStamp) != 0) return timeStamp.compareTo(o.timeStamp);
        return sequence.compareTo(o.sequence);
    }

    public static String parseStreamId(String id) {
        AssertUtil.assertTrue(id.matches("\\d+-(\\d+|\\*?)|\\d+|\\*|\\+|\\-"), "ERROR: ID is invalid: " + id);
        if (id.matches("\\-")) id = "0-0";
        if (id.matches("\\+")) id = Long.MAX_VALUE + "-" + Integer.MAX_VALUE;
        if (id.matches("\\*")) id = System.currentTimeMillis() + "-0";
        if (id.matches("\\d+")) id = id + "-0";
        if (id.matches("\\d+-\\*")) id = id.replace("*", "0");
        if (id.matches("\\d+-")) id = id + "0";
        return id;
    }

    public static String incrementIdSequence(String id) {
        StreamId streamId = new StreamId(parseStreamId(id));
        return streamId.getTimeStamp() + "-" + (streamId.getSequence() + 1);
    }

}
