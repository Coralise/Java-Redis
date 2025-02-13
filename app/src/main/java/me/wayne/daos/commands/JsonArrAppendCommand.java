package me.wayne.daos.commands;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.JedisJson;
import me.wayne.daos.storevalues.StoreValue;

public class JsonArrAppendCommand extends AbstractCommand<Integer> {

    public JsonArrAppendCommand() {
        super("JSON.ARRAPPEND", 3);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        String path = args.get(1);
        List<String> values = args.subList(2, args.size());

        StoreValue storeValue = store.getStoreValue(key, true);
        JedisJson redisJson = storeValue.getValue(JedisJson.class);

        return redisJson.arrayAppend(path, values.toArray(new String[0]));
    }
    
}
