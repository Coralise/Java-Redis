package me.wayne.daos.commands;

import java.util.List;
import java.util.SortedMap;

import com.fasterxml.jackson.databind.JsonNode;

import me.wayne.InMemoryStore;
import me.wayne.daos.RedisJson;

public class JsonGetCommand extends AbstractCommand<String> {

    public JsonGetCommand() {
        super("JSON.GET", 2);
    }

    @Override
    protected String processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        List<String> paths = args.subList(1, args.size());

        RedisJson redisJson = store.getObject(key, RedisJson.class);
        if (redisJson == null) throw new IllegalArgumentException("JSON Object does not exist for key: " + key);

        SortedMap<String, JsonNode> map = redisJson.get(paths.toArray(new String[0]));
        if (map.size() == 1) {
            JsonNode entry = map.firstEntry().getValue();
            return entry.toString();
        }else return map.toString();
    }
    
}
