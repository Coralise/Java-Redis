package me.wayne.daos.commands;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.nosql.Document;
import me.wayne.daos.nosql.DocumentCollection;

public class CollectionAddCommand extends AbstractCommand<String> {

    // COLLECTION.ADD key field1 value1 field2 value2 ...
    public CollectionAddCommand() {
        super("COLLECTION.ADD", 1);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        List<String> keysAndValues = args.size() > 1 ? args.subList(1, args.size()) : null;
        DocumentCollection collection = store.getStoreValue(key, DocumentCollection.class, true);
        Document document = keysAndValues != null ? new Document(keysAndValues) : new Document();
        collection.add(document);
        store.setStoreValue(key, collection, inputLine);
        return OK_RESPONSE;
    }
    
}
