package me.wayne.daos.commands;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.nosql.Document;
import me.wayne.daos.nosql.DocumentCollection;
import me.wayne.daos.storevalues.PrintableList;

public class CollectionSearchCommand extends AbstractCommand<List<Document>> {

    // COLLECTION.SEARCH key QUERY fieldPath value
    // COLLECTION.SEARCH key INDEX QUERY indexName value
    public CollectionSearchCommand() {
        super("COLLECTION.SEARCH", 4, 5);
    }

    @Override
    protected List<Document> processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        
        String key = args.get(0);
        DocumentCollection collection = store.getStoreValue(key, DocumentCollection.class, true);

        boolean isIndex = args.get(1).equals("INDEX");

        List<Document> result;
        if (isIndex) {
            String indexName = args.get(3);
            String value = args.get(4);
            result = collection.getIndex(indexName).search(value);
        } else {
            String fieldPath = args.get(2);
            String value = args.get(3);
            result = collection.search(fieldPath, value);
        }
        
        return new PrintableList<>(result);
    }
    
}
