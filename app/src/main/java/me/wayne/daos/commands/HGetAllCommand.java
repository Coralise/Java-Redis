package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.wayne.InMemoryStore;

public class HGetAllCommand extends AbstractCommand<List<String>> {

    public HGetAllCommand() {
        super("HGETALL", 1, 1);
    }

    @Override
    protected List<String> processCommand(InMemoryStore store, List<String> args) {
        String key = args.get(0);
        if (!store.getStore().containsKey(key)) return new ArrayList<>();
        Map<String, String> hashMap = getMap(store, key);
        List<String> fieldsAndValues = new ArrayList<>();
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            fieldsAndValues.add(entry.getKey());
            fieldsAndValues.add(entry.getValue());
        }
        return fieldsAndValues;
    }
    
}
