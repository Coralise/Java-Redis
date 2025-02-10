package me.wayne.daos.commands;

import java.util.List;

import me.wayne.InMemoryStore;
import me.wayne.daos.RedisJson;

public class JsonArrAppendCommand extends AbstractCommand<Integer> {

    public JsonArrAppendCommand() {
        super("JSON.ARRAPPEND", 3);
    }

    @Override
    protected Integer processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        String path = args.get(1);
        List<String> values = args.subList(2, args.size());

        RedisJson redisJson = store.getObject(key, RedisJson.class);
        if (redisJson == null) throw new IllegalArgumentException("JSON Object does not exist for key: " + key);

        return redisJson.arrayAppend(path, values.toArray(new String[0]));
    }
    
}
