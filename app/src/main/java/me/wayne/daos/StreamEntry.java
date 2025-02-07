package me.wayne.daos;

import java.util.List;

public class StreamEntry {
    private final String id;
    private final List<String> fieldsAndValues;

    public StreamEntry(String id, List<String> fields) {
        this.id = id;
        this.fieldsAndValues = fields;
    }

    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return Long.parseLong(id.split("-")[0]);
    }

    public int getSequence() {
        return Integer.parseInt(id.split("-")[1]);
    }

    public List<String> getFieldsAndValues() {
        return fieldsAndValues;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((fieldsAndValues == null) ? 0 : fieldsAndValues.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass()) {
            return obj instanceof String && id.equals(obj);
        }
        StreamEntry other = (StreamEntry) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (fieldsAndValues == null) {
            if (other.fieldsAndValues != null)
                return false;
        } else if (!fieldsAndValues.equals(other.fieldsAndValues))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "[" + id + ", " + fieldsAndValues + "]";
    }

    
}