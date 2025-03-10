package me.wayne.daos.storevalues.streams;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nonnull;

import me.wayne.daos.Printable;
import me.wayne.daos.storevalues.PrintableList;

public class StreamEntry implements Comparable<StreamEntry>, Serializable, Printable {
    private static final long serialVersionUID = 1L;

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
    public int compareTo(StreamEntry o) {
        return id.compareTo(o.id);
    }

    @Override
    public String toPrint(int indent) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("1) " + id.toString() + "\n");
        for (int i = 0;i < indent*3;i++) stringBuilder.append(" ");
        stringBuilder.append("2) " + new PrintableList<>(fieldsAndValues, indent + 1));
        return stringBuilder.toString();
    }

}