package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;

public class MultiCommand extends AbstractCommand<String> {

    public MultiCommand() {
        super("MULTI", 0, 0);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        boolean res = store.createTransaction(Thread.currentThread());
        return res ? "+" + OK_RESPONSE : "-ERR MULTI calls can't be nested";
    }
    
}
