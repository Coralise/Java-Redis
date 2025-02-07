package me.wayne.daos;

public class StreamId {

    private final Long timeStamp;
    private final int sequence;

    public StreamId(Long timeStamp, int sequence) {
        this.timeStamp = timeStamp;
        this.sequence = sequence;
    }

    public StreamId(String id) {
        if (!id.matches("\\d+-\\d+")) throw new IllegalArgumentException("ERROR: Invalid ID format");
        this.timeStamp = Long.parseLong(id.split("-")[0]);
        this.sequence = Integer.parseInt(id.split("-")[1]);
    }

    public String getId() {
        return timeStamp + "-" + sequence;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public int getSequence() {
        return sequence;
    }

    public static boolean isValidId(String id) {
        return id.matches("\\d+-\\d+");
    }

    public static boolean isValidPartialId(String id) {
        return id.matches("\\d+");
    }

    @Override
    public String toString() {
        return getId();
    }

}
