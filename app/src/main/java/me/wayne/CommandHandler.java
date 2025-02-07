package me.wayne;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.wayne.daos.commands.*;

public class CommandHandler implements Runnable {
    private static final String ERROR_UNKOWN_COMMAND = "ERROR: Unkown Command";
    private Socket clientSocket;
    private InMemoryStore dataStore;
    private Map<String, AbstractCommand<?>> commands = new HashMap<>();

    public InMemoryStore getDataStore() {
        return dataStore;
    }

    public CommandHandler(Socket clientSocket, InMemoryStore dataStore) {
        this.clientSocket = clientSocket;
        this.dataStore = dataStore;

        commands.put("APPEND", new AppendCommand());
        commands.put("DECRBY", new DecrByCommand());
        commands.put("DECR", new DecrCommand());
        commands.put("DELETE", new DeleteCommand());
        commands.put("GET", new GetCommand());
        commands.put("GETRANGE", new GetRangeCommand());
        commands.put("HDEL", new HDelCommand());
        commands.put("HEXISTS", new HExistsCommand());
        commands.put("HGETALL", new HGetAllCommand());
        commands.put("HGET", new HGetCommand());
        commands.put("HSET", new HSetCommand());
        commands.put("INCRBY", new IncrByCommand());
        commands.put("INCR", new IncrCommand());
        commands.put("LINDEX", new LIndexCommand());
        commands.put("LPOP", new LPopCommand());
        commands.put("LPUSH", new LPushCommand());
        commands.put("LRANGE", new LRangeCommand());
        commands.put("LSET", new LSetCommand());
        commands.put("RPOP", new RPopCommand());
        commands.put("RPUSH", new RPushCommand());
        commands.put("SADD", new SAddCommand());
        commands.put("SDIFF", new SDiffCommand());
        commands.put("SET", new SetCommand());
        commands.put("SETRANGE", new SetRangeCommand());
        commands.put("SINTER", new SInterCommand());
        commands.put("SISMEMBER", new SIsMemberCommand());
        commands.put("SMEMBERS", new SMembersCommand());
        commands.put("SREM", new SRemCommand());
        commands.put("STRLEN", new StrLenCommand());
        commands.put("SUNION", new SUnionCommand());
        commands.put("XADD", new XAddCommand());
        commands.put("XRANGE", new XRangeCommand());
        commands.put("XREAD", new XReadCommand());
        commands.put("ZADD", new ZAddCommand());
        commands.put("ZRANGE", new ZRangeCommand());
        commands.put("ZRANK", new ZRankCommand());
        commands.put("ZREM", new ZRemCommand());
        
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                AbstractCommand<?> command = commands.get(inputLine.split(" ")[0].toUpperCase());
                if (command != null) {
                    try {
                        out.println(command.executeCommand(dataStore, inputLine));
                    } catch (Exception e) {
                        out.println(e.getMessage());
                        System.out.println(e.getMessage());
                        System.err.println(Arrays.toString(e.getStackTrace()));
                    }
                } else {
                    out.println(ERROR_UNKOWN_COMMAND);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getArgs(String input) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                result.add(matcher.group(1));
            } else {
                result.add(matcher.group(2));
            }
        }

        return result;
    }

}