package me.wayne.daos.commands;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.nosql.DocumentCollection;

public class CollectionIndexCommand extends AbstractCommand<String> {

    // COLLECTION.INDEX key indexName field
    public CollectionIndexCommand() {
        super("COLLECTION.INDEX", 3, 3);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        DocumentCollection collection = store.getStoreValue(key, DocumentCollection.class, true);

        String indexName = args.get(1);
        String field = args.get(2);
        collection.createIndex(indexName, field);
        store.setStoreValue(key, collection, inputLine);
        return OK_RESPONSE;
    }
    
}
