package me.wayne.daos.commands;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import me.wayne.AssertUtil;
import me.wayne.daos.Printable;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.nosql.Document;
import me.wayne.daos.nosql.DocumentCollection;

public class CollectionGetCommand extends AbstractCommand<String> {

    // COLLECTION.GET key documentIndex fieldPath
    public CollectionGetCommand() {
        super("COLLECTION.GET", 2, 3);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        int documentIndex = Integer.parseInt(args.get(1));
        String fieldPath = args.size() > 2 ? args.get(2) : null;
        DocumentCollection collection = store.getStoreValue(key, DocumentCollection.class, true);
        AssertUtil.assertTrue(documentIndex >= 0 && documentIndex < collection.size(), "ERR document index out of range");
        Document document = collection.get(documentIndex);
        Serializable value = fieldPath != null ? document.getValue(fieldPath) : document;
        return value instanceof Printable printable ? printable.toPrint(0) : value.toString();
    }
    
}
