package me.wayne.daos;

import java.util.ArrayList;
import java.util.UUID;

import javax.annotation.Nullable;

import me.wayne.daos.commands.AbstractCommand;
import me.wayne.daos.io.StorePrintWriter;

public class Transaction {
 
    private ArrayList<String> commands = new ArrayList<>();

    public String addCommand(String commandString) {
        AbstractCommand<Object> command = Commands.getCommand(commandString.split(" ")[0]);
        if (command == null) return "-ERR Unknown Command";
        if (!command.isArgumentCountValid(commandString)) return "-ERR Wrong number of arguments for '  " + command.getCommand() + "' command";
        commands.add(commandString);
        return "+QUEUED";
    }

    public void execute(StorePrintWriter out, @Nullable UUID requestUuid) {
        
        ArrayList<String> results = new ArrayList<>();
        for (String command : commands) {
            AbstractCommand<Object> commandObj = Commands.getCommand(command.split(" ")[0]);
            Object res;
            try {
                res = commandObj.executeCommand(out, requestUuid, command, false);
            } catch (Exception e) {
                res = "-" + e.getMessage();
            }
            results.add(res.toString());
        }

        out.println(requestUuid, results.toString());

    }

}