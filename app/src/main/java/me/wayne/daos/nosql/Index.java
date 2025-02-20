package me.wayne.daos.nosql;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class Index extends HashMap<Serializable, DocumentCollection> {

    public void indexField(String fieldPath, DocumentCollection documents) {
        for (Document doc : documents) indexField(fieldPath, doc);
    }

    public void indexField(String fieldPath, Document document) {
        Serializable value = document.getValue(fieldPath);
        putIfAbsent(value, new DocumentCollection());
        get(value).add(document);
    }

    public List<Document> search(Serializable value) {
        return getOrDefault(value, new DocumentCollection());
    }

}
