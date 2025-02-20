package me.wayne.daos.nosql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentCollection extends ArrayList<Document> {

    private final Map<String, Index> indexes = new HashMap<>();

    public List<Document> search(String fieldPath, Object value) {
        List<Document> results = new ArrayList<>();
        for (Document doc : this) {
            if (doc.getValue(fieldPath) != null && doc.getValue(fieldPath).equals(value)) {
                results.add(doc);
            }
        }
        return results;
    }

    public int count() {
        return size();
    }

    public double sum(String field) {
        return stream().mapToDouble(doc -> (double) doc.get(field)).sum();
    }

    public double avg(String field) {
        return sum(field) / count();
    }
    
    public Index getIndex(String indexName) {
        return indexes.get(indexName);
    }

    public void createIndex(String indexName, String fieldPath) {
        Index index = new Index();
        for (Document doc : this) {
            index.indexField(fieldPath, doc);
        }
        indexes.put(indexName, index);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + indexes.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DocumentCollection other = (DocumentCollection) obj;
        return indexes.equals(other.indexes);
    }
    
}
