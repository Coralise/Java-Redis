package me.wayne.daos.commands;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.nosql.DocumentCollection;

public class CollectionCreateCommand extends AbstractCommand<String> {

    public CollectionCreateCommand() {
        super("COLLECTION.CREATE", 1, 1);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        DocumentCollection collection = new DocumentCollection();
        store.setStoreValue(key, collection, inputLine);
        return OK_RESPONSE;
    }
    
}
