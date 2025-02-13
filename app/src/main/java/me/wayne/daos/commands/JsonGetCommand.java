package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;
import java.util.SortedMap;

import com.fasterxml.jackson.databind.JsonNode;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.JedisJson;
import me.wayne.daos.storevalues.StoreValue;

public class JsonGetCommand extends AbstractCommand<String> {

    public JsonGetCommand() {
        super("JSON.GET", 2);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        List<String> paths = args.subList(1, args.size());

        StoreValue storeValue = store.getStoreValue(key, true);
        JedisJson redisJson = storeValue.getValue(JedisJson.class);

        SortedMap<String, JsonNode> map = redisJson.get(paths.toArray(new String[0]));
        if (map.size() == 1) {
            JsonNode entry = map.firstEntry().getValue();
            return entry.toString();
        }else return map.toString();
    }
    
}
