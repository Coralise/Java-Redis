package me.wayne.daos.nosql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.wayne.AssertUtil;
import me.wayne.daos.Printable;
import me.wayne.daos.storevalues.PrintableList;

public class Document extends LinkedHashMap<String, Serializable> implements Printable {

    public Document(List<String> keysAndValues) {
        AssertUtil.assertTrue(keysAndValues.size() % 2 == 0, "Keys and values must have the same size");
        for (int i = 1; i < keysAndValues.size(); i+=2) put(keysAndValues.get(i-1), keysAndValues.get(i));    
    }

    public Document() {
        super();
    }

    public Document getChildDocument(String fieldPath) {
        return getChildDocument(this, fieldPath);
    }

    @SuppressWarnings("unchecked")
    public Document getChildDocument(Document parentDocument, String fieldPath) {
        String[] tokens = fieldPath.split("\\.");
        String token = tokens[0];
        Document child;

        if (token.contains("[") && token.contains("]")) {
            // Handle arrays (e.g., "addresses[1]")
            String fieldName = token.substring(0, token.indexOf("["));
            int index = Integer.parseInt(token.substring(token.indexOf("[") + 1, token.indexOf("]")));

            Object listObj = parentDocument.get(fieldName);
            if (listObj instanceof List) {
                child = (Document) ((List<Serializable>) listObj).get(index);
            } else {
                throw new IllegalArgumentException("Invalid array access: " + token);
            }
        } else {
            // Handle normal objects (e.g., "user")
            AssertUtil.assertTrue(parentDocument.get(token) instanceof Document, "ERR Field " + token + " is not a document");
            child = (Document) parentDocument.get(token);
        }

        if (tokens.length > 1) {
            return getChildDocument(child, fieldPath.substring(fieldPath.indexOf(".") + 1));
        } else {
            return child;
        }
    }

    @SuppressWarnings("unchecked")
    public void updateField(String fieldPath, Serializable value) {
        String[] tokens = fieldPath.split("\\.");

        Document childDocument = tokens.length > 1 ? getChildDocument(this, fieldPath.substring(0, fieldPath.lastIndexOf("."))) : this;

        // Set the value in the last field
        String field = tokens[tokens.length - 1];
        if (field.contains("[") && field.contains("]")) {
            int index = Integer.parseInt(field.substring(field.indexOf("[") + 1, field.indexOf("]")));
            field = field.substring(0, field.indexOf("["));
            
            Object listObj = childDocument.get(field);
            if (listObj instanceof List) {
                List<Serializable> list = ((List<Serializable>) listObj);
                list.set(index, value);
            } else {
                throw new IllegalArgumentException("Invalid array access: " + field);
            }
        } else {
            if (value != null) childDocument.put(field, value);
            else childDocument.remove(field);
        }
    }

    @SuppressWarnings("unchecked")
    public Serializable getValue(String fieldPath) {
        String[] tokens = fieldPath.split("\\.");

        Document childDocument = tokens.length > 1 ? getChildDocument(this, fieldPath.substring(0, fieldPath.lastIndexOf("."))) : this;

        Serializable value = null;
        // Get the value in the last field
        String field = tokens[tokens.length - 1];
        if (field.contains("[") && field.contains("]")) {
            int index = Integer.parseInt(field.substring(field.indexOf("[") + 1, field.indexOf("]")));
            field = field.substring(0, field.indexOf("["));
            
            Object listObj = childDocument.get(field);
            if (listObj instanceof List) {
                value = ((List<Serializable>) listObj).get(index);
            } else {
                throw new IllegalArgumentException("Invalid array access: " + field);
            }
        } else {
            value = childDocument.get(field);
        }

        return value instanceof Collection ? new PrintableList<Serializable>((Collection<Serializable>) value) : value;
    }

    @SuppressWarnings("unchecked")
    public List<Serializable> getList(String field) {
        return (ArrayList<Serializable>) get(field);
    }

    @Override
    public String toPrint(int indent) {
        PrintableList<String> keysAndValues = new PrintableList<>();
        keysAndValues.setIndent(indent);
        for (Map.Entry<String, Serializable> entry : entrySet()) {
            keysAndValues.add(entry.getKey());
            if (entry.getValue() instanceof Collection) {
                keysAndValues.add(new PrintableList<>((Collection<?>) entry.getValue(), indent + 1).toString());
            } else if (entry.getValue() instanceof Printable printable) {
                keysAndValues.add(printable.toPrint(indent + 1));
            } else {
                keysAndValues.add(entry.getValue().toString());
            }
        }
        return keysAndValues.toString();
    }

}