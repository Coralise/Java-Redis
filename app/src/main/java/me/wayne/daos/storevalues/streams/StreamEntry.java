package me.wayne.daos.storevalues.streams;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nonnull;

public class StreamEntry implements Comparable<StreamEntry>, Serializable {

    @Nonnull
    private final StreamId id;

    private final List<String> fieldsAndValues;

    public StreamEntry(StreamId id, List<String> fields) {
        this.id = id;
        this.fieldsAndValues = fields;
    }

    public StreamEntry(StreamId id) {
        this(id, null);
    }

    public List<String> getFieldsAndValues() {
        return fieldsAndValues;
    }
    
    public StreamId getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        StreamEntry other = (StreamEntry) obj;
        return id.equals(other.id);
    }

    @Override
    public String toString() {
        return "[" + id.toString() + ", " + fieldsAndValues + "]";
    }

    @Override
    public int compareTo(StreamEntry o) {
        return id.compareTo(o.id);
    }

}