package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.RedisJson;
import me.wayne.daos.StoreValue;

public class JsonArrAppendCommand extends AbstractCommand<Integer> {

    public JsonArrAppendCommand() {
        super("JSON.ARRAPPEND", 3);
    }

    @Override
    protected Integer processCommand(PrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        String path = args.get(1);
        List<String> values = args.subList(2, args.size());

        StoreValue storeValue = store.getStoreValue(key, true);
        RedisJson redisJson = storeValue.getValue(RedisJson.class);

        return redisJson.arrayAppend(path, values.toArray(new String[0]));
    }
    
}
