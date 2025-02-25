package me.wayne.daos.commands;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.vectors.VectorDB;

public class VCreateCommand extends AbstractCommand<String> {

    public VCreateCommand() {
        super("VCREATE", 2, 2);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        int dimensions = Integer.parseInt(args.get(1));
        VectorDB vectorDb = new VectorDB(dimensions);
        store.setStoreValue(key, vectorDb, inputLine);
        return OK_RESPONSE;
    }
    
}
