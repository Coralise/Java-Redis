package me.wayne.daos.commands;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.JedisJson;
import me.wayne.daos.storevalues.StoreValue;

public class JsonDelCommand extends AbstractCommand<Integer> {

    public JsonDelCommand() {
        super("JSON.DEL", 2, 2);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        String path = args.get(1);

        StoreValue storeValue = store.getStoreValue(key, true);
        JedisJson redisJson = storeValue.getValue(JedisJson.class);

        return redisJson.delete(path);
    }
    
}
