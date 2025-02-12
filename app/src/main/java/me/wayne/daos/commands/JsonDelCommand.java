package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.RedisJson;
import me.wayne.daos.StoreValue;
import me.wayne.daos.io.StorePrintWriter;

public class JsonDelCommand extends AbstractCommand<Integer> {

    public JsonDelCommand() {
        super("JSON.DEL", 2, 2);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        String path = args.get(1);

        StoreValue storeValue = store.getStoreValue(key, true);
        RedisJson redisJson = storeValue.getValue(RedisJson.class);

        return redisJson.delete(path);
    }
    
}
