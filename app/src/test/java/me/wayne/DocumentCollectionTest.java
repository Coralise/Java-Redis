package me.wayne;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import me.wayne.daos.nosql.Document;
import me.wayne.daos.nosql.DocumentCollection;

class DocumentCollectionTest {

    private Document getDefaultDocument() {
        Document document = new Document();

        document.updateField("Field1", "Value1");
        assertEquals("Value1", document.get("Field1"));

        document.updateField("Field2", new Document());
        assertEquals(new Document(), document.get("Field2"));

        document.updateField("Field2.Field1", "Value2 Value1");
        assertEquals("Value2 Value1", document.getChildDocument("Field2").get("Field1"));

        document.updateField("Field2.Field2", new Document());
        assertEquals(new Document(), document.getValue("Field2.Field2"));

        document.updateField("Field2.Field2.Field3", new ArrayList<>(List.of("Value1" , new Document(), "Value3")));

        document.updateField("Field3", "indexValue1");

        return document;
    }

    @Test
    void documentCollectionTest() {

        DocumentCollection collection = new DocumentCollection();

        Document document = getDefaultDocument();
        collection.add(document);
        
        assertEquals("Value1", document.getValue("Field2.Field2.Field3[0]"));
        assertEquals(new Document(), document.getValue("Field2.Field2.Field3[1]"));
        assertEquals("Value3", document.getValue("Field2.Field2.Field3[2]"));

        document.updateField("Field2.Field2.Field3[1].Field1", "Value1");
        assertEquals("Value1", document.getValue("Field2.Field2.Field3[1].Field1"));

        document.updateField("Field2.Field2.Field3[2]", "New Value3");
        assertEquals("New Value3", document.getValue("Field2.Field2.Field3[2]"));

        document.updateField("Field2.Field2.Field3[2]", new Document());
        assertEquals(new Document(), document.getValue("Field2.Field2.Field3[2]"));

        document.updateField("Field2.Field2", null);
        assertEquals(null, document.getValue("Field2.Field2"));

    }

    @Test
    void collectionSearchTest() {

        DocumentCollection collection = new DocumentCollection();
        collection.add(getDefaultDocument());
        collection.add(getDefaultDocument());
        collection.add(getDefaultDocument());
        collection.add(getDefaultDocument());

        collection.createIndex("index1", "Field3");
        assertEquals(4, collection.getIndex("index1").search("indexValue1").size());

        assertEquals(4, collection.search("Field2.Field2.Field3[2]", "Value3").size());

    }
    
}
