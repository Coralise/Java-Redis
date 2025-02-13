package me.wayne.daos.commands;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.JedisJson;

public class JsonSetCommand extends AbstractCommand<String> {

    public JsonSetCommand() {
        super("JSON.SET", 3, 4);
    }

    @Override
    protected String processCommand(StorePrintWriter out, List<String> args) {
        String key = args.get(0);
        String path = args.get(1);
        String value = args.get(2);
        boolean nx = args.size() == 4 && args.get(3).equalsIgnoreCase("NX");
        boolean xx = args.size() == 4 && args.get(3).equalsIgnoreCase("XX");

        JedisJson redisJson = store.getStoreValue(key, JedisJson.class, new JedisJson());

        String response = redisJson.set(path, value, xx, nx) ? OK_RESPONSE : null;

        store.setStoreValue(key, redisJson);
        return response;
    }
    
}
