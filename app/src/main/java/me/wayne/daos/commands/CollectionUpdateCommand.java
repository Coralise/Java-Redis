package me.wayne.daos.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import me.wayne.AssertUtil;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.nosql.Document;
import me.wayne.daos.nosql.DocumentCollection;

public class CollectionUpdateCommand extends AbstractCommand<String> {

    // COLLECTION.UPDATE key LIST documentIndex fieldPath value1 value2 value3 ...
    // COLLECTION.UPDATE key DOC documentIndex fieldPath field1 value1 field2 value2 ...
    // COLLECTION.UPDATE key DELETE documentIndex fieldPath
    // COLLECTION.UPDATE key documentIndex fieldPath value
    public CollectionUpdateCommand() {
        super("COLLECTION.UPDATE", 4);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        DocumentCollection collection = store.getStoreValue(key, DocumentCollection.class, true);

        boolean isList = "LIST".equals(args.get(1));
        boolean isDoc = "DOC".equals(args.get(1));
        boolean isDelete = "DELETE".equals(args.get(1));

        int documentIndex = getValueAsInteger(args.get(isList || isDoc ? 2 : 1));
        String fieldPath = args.get(isList || isDoc ? 3 : 2);
        AssertUtil.assertTrue(documentIndex >= 0 && documentIndex < collection.size(), "ERR no such document");
        Document document = collection.get(documentIndex);

        if (isList) {
            ArrayList<Serializable> list = new ArrayList<>(args.subList(4, args.size()));
            document.updateField(fieldPath, list);
        } else if (isDoc) {
            Document childDocument = args.size() > 4 ? new Document(args.subList(4, args.size())) : new Document();
            document.updateField(fieldPath, childDocument);
        } else if (isDelete) {
            document.updateField(fieldPath, null);
        } else {
            Serializable value = args.get(3);
            document.updateField(fieldPath, value);
        }

        store.setStoreValue(key, collection, inputLine);
        return OK_RESPONSE;
    }
    
}
