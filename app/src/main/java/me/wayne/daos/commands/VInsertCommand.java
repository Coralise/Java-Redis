package me.wayne.daos.commands;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.vectors.VectorDB;

public class VInsertCommand extends AbstractCommand<String> {

    public VInsertCommand() {
        super("VINSERT", 3);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        String field = args.get(1);
        double[] values = args.subList(2, args.size()).stream().mapToDouble(Double::parseDouble).toArray();
        
        VectorDB vectorDB = store.getStoreValue(key, VectorDB.class, new VectorDB(values.length));
        if (vectorDB.getDimensions() != values.length) return "ERR dimensions mismatch";

        vectorDB.insert(field, values);
        store.setStoreValue(key, vectorDB, inputLine);

        return OK_RESPONSE;
    }
    
}
