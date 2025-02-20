package me.wayne.daos.commands;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import me.wayne.AssertUtil;
import me.wayne.daos.Pair;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.vectors.VectorDB;

public class VSearchCoSimCommand extends AbstractCommand<List<Pair<String, Double>>> {

    public VSearchCoSimCommand() {
        super("VSEARCH.COSIM", 2);
    }

    @Override
    protected List<Pair<String, Double>> processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        int k = Integer.parseInt(args.get(1));
        double[] values = args.subList(2, args.size()).stream().mapToDouble(Double::parseDouble).toArray();

        VectorDB vectorDB = store.getStoreValue(key, VectorDB.class, true);

        AssertUtil.assertTrue(vectorDB.getDimensions() == values.length, "ERR dimensions mismatch");

        return vectorDB.findKNearestNeighborsoUsingCosineSimilarity(values, k);
    }
    
}
