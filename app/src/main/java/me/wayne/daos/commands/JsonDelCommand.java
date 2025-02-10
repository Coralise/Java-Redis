package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.RedisJson;

public class JsonDelCommand extends AbstractCommand<Integer> {

    public JsonDelCommand() {
        super("JSON.DEL", 2, 2);
    }

    @Override
    protected Integer processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        String path = args.get(1);

        RedisJson redisJson = store.getObject(key, RedisJson.class);
        if (redisJson == null) throw new IllegalArgumentException("JSON Object does not exist for key: " + key);

        return redisJson.delete(path);
    }
    
}
