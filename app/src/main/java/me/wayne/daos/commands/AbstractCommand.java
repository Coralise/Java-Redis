package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import me.wayne.InMemoryStore;
import me.wayne.daos.io.StorePrintWriter;

public abstract class AbstractCommand<T> {
    
    protected final Logger logger = Logger.getLogger(this.getClass().getName());
    protected static final String INVALID_ARGS_RESPONSE = "ERR Invalid number of arguments";
    protected static final String OK_RESPONSE = "OK";
    protected static final String NON_INTEGER_ERROR_MSG = "ERR Value is not of type Integer";
    protected static final String NON_STRING_ERROR_MSG = "ERR Value is not of type String";
    protected static final String KEY_DOESNT_EXIST_MSG = "ERR Key does not exist";
    protected static final String NON_LIST_ERROR_MSG = "ERR Value is not of type List";
    protected static final String NON_JSON_ERROR_MSG = "ERR Value is not of type JSONObject or JSONArray";
    protected static final String INDEX_OUT_OF_RANGE_MSG = "ERR Index out of range";

    protected final String command;

    private final int minArgs;
    private final int maxArgs;
    private final boolean printOutput;

    protected final InMemoryStore store = InMemoryStore.getInstance();

    protected AbstractCommand(String command, int minArgs) {
        this(command, minArgs, -1, true);
    }

    protected AbstractCommand(String command, int minArgs, boolean printOutput) {
        this(command, minArgs, -1, printOutput);
    }

    protected AbstractCommand(String command, int minArgs, int maxArgs) {
        this(command, minArgs, maxArgs, true);
    }

    protected AbstractCommand(String command, int minArgs, int maxArgs, boolean printOutput) {
        this.command = command;
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.printOutput = printOutput;
    }
    
    public String getCommand() {
        return command;
    }

    public boolean isArgumentCountValid(String command) {
        List<String> args = getArgs(command);
        return args.size() >= minArgs && (maxArgs == -1 || args.size() <= maxArgs);
    }
    
    public T executeCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine) {
        return executeCommand(out, requestUuid, inputLine, null);
    }

    public T executeCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, Boolean overridePrintOutput) {

        List<String> args = getArgs(inputLine);
        if (args.size() < minArgs || (maxArgs != -1 && args.size() > maxArgs)) {
            throw new IllegalArgumentException(INVALID_ARGS_RESPONSE);
        }
        T res = processCommand(out, requestUuid, inputLine, args);
        if (overridePrintOutput == null) {
            if (printOutput) out.println(requestUuid, res);
        } else {
            if (overridePrintOutput) out.println(requestUuid, res);
        }

        return res;
    }

    protected abstract T processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args);

    private List<String> getArgs(String input) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("[^\\s\"]+|\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String group1 = matcher.group(1);
            if (group1 != null) {
                group1 = group1.replace("\\\"", "\"");
                result.add(group1);
            } else {
                String group0 = matcher.group();
                group0 = group0.replace("\\\"", "\"");
                result.add(group0);
            }
        }

        result.removeFirst();

        return result;
    }

    protected int getValueAsInteger(Object obj) {
        return switch (obj) {
            case Integer integer -> integer;
            case String string -> Integer.parseInt(string);
            default -> throw new AssertionError(NON_INTEGER_ERROR_MSG);
        };
    }
    
}
