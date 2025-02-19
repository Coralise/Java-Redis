package me.wayne.daos.storevalues;

import java.util.HashMap;
import java.util.Map;

public class StoreMap extends HashMap<String, String> {

    public PrintableList<String> getFieldsAndValues() {
        PrintableList<String> fieldsAndValues = new PrintableList<>();
        for (Map.Entry<String, String> entry : entrySet()) {
            fieldsAndValues.add(entry.getKey());
            fieldsAndValues.add(entry.getValue());
        }
        return fieldsAndValues;
    }

}