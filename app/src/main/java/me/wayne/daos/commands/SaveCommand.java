package me.wayne.daos.commands;

import java.util.List;

import me.wayne.PersistenceManager;
import me.wayne.daos.io.StorePrintWriter;

public class SaveCommand extends AbstractCommand<String> {

    public SaveCommand() {
        super("SAVE", 0);
    }

    @Override
    protected String processCommand(StorePrintWriter out, List<String> args) {
        PersistenceManager.saveStore();
        return OK_RESPONSE;
    }
    
}
