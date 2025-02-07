package me.wayne.daos;

public class StreamId {

    private final Long timeStamp;
    private final int sequence;

    public StreamId(Long timeStamp, int sequence) {
        this.timeStamp = timeStamp;
        this.sequence = sequence;
    }

    public StreamId(String id) {
        if (id.matches("\\d+")) id = id + "-0";
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
        result = prime * result + sequence;
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
        if (timeStamp == null) {
            if (other.timeStamp != null)
                return false;
        } else if (!timeStamp.equals(other.timeStamp))
            return false;
        return sequence == other.sequence;
    }

}
