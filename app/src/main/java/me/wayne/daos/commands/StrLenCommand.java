package me.wayne.daos.commands;

import java.util.List;

import me.wayne.AssertUtil;
import me.wayne.InMemoryStore;

public class StrLenCommand extends AbstractCommand<Integer> {

    public StrLenCommand() {
        super("STRLEN", 1, 1);
    }

    @Override
    protected Integer processCommand(InMemoryStore store, List<String> args) {
        String key = args.get(0);
        AssertUtil.assertTrue(store.getStore().containsKey(key), KEY_DOESNT_EXIST_MSG);
        AssertUtil.assertTrue(store.getStore().get(key) instanceof CharSequence, "Value is not of type CharSequence");
        return ((CharSequence) store.getStore().get(key)).length();
    }
    
}
