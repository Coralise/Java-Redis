package me.wayne.daos.commands;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import me.wayne.AssertUtil;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.vectors.VectorDB;
import me.wayne.daos.vectors.VectorOperations;

public class VDotCommand extends AbstractCommand<String> {

    public VDotCommand() {
            super("VDOT", 3);
        }
    
        @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        VectorDB vectorDb = store.getStoreValue(key, VectorDB.class, true);

        double[] id1 = vectorDb.get(args.get(1));
        double[] id2 = vectorDb.get(args.get(2));

        AssertUtil.assertTrue(id1 != null, "ERR Record with ID " + id1 + " does not exist");
        AssertUtil.assertTrue(id2 != null, "ERR Record with ID " + id2 + " does not exist");

        return VectorOperations.dotProduct(id1, id2).toString();
    }
    
}
