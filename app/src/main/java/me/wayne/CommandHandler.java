package me.wayne;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.wayne.daos.commands.*;
import me.wayne.daos.io.StoreBufferedReader;
import me.wayne.daos.io.StorePrintWriter;

public class CommandHandler implements Runnable {

    private static final Map<String, AbstractCommand<?>> commands = new HashMap<>();
    static {
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
        commands.put("XGROUP", new XGroupCommand());
        commands.put("XREADGROUP", new XReadGroupCommand());
        commands.put("XACK", new XAckCommand());
        commands.put("GEOADD", new GeoAddCommand());
        commands.put("GEOSEARCH", new GeoSearchCommand());
        commands.put("GEODIST", new GeoDistCommand());
        commands.put("SETBIT", new SetBitCommand());
        commands.put("GETBIT", new GetBitCommand());
        commands.put("BITCOUNT", new BitCountCommand());
        commands.put("BITOP", new BitOpCommand());
        commands.put("EXISTS", new ExistsCommand());
        commands.put("PFADD", new PfAddCommand());
        commands.put("PFCOUNT", new PfCountCommand());
        commands.put("PFMERGE", new PfMergeCommand());
        commands.put("TS.CREATE", new TsCreateCommand());
        commands.put("TS.ADD", new TsAddCommand());
        commands.put("TS.RANGE", new TsRangeCommand());
        commands.put("TS.GET", new TsGetCommand());
        commands.put("JSON.SET", new JsonSetCommand());
        commands.put("JSON.GET", new JsonGetCommand());
        commands.put("JSON.DEL", new JsonDelCommand());
        commands.put("JSON.ARRAPPEND", new JsonArrAppendCommand());
        commands.put("BITFIELD", new BitFieldCommand());
    }

    private static final Logger logger = Logger.getLogger(CommandHandler.class.getName());
    private static final String INPUT_PREFIX = ">> ";

    private Socket clientSocket;
    private InMemoryStore dataStore;


    public InMemoryStore getDataStore() {
        return dataStore;
    }

    public CommandHandler(Socket clientSocket, InMemoryStore dataStore) {
        this.clientSocket = clientSocket;
        this.dataStore = dataStore;
    }

    @Override
    public void run() {
        try (StoreBufferedReader in = new StoreBufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             StorePrintWriter out = new StorePrintWriter(clientSocket.getOutputStream())) {

            out.println("----------------------------");
            out.println("Connected to Jedis. Welcome!");
            out.println("----------------------------");
            out.println();
            out.print(INPUT_PREFIX);
            out.flush();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("CommandHandler received: " + inputLine);

                UUID requestUuid = null;
                String[] inputLineSplit = inputLine.split(":");
                if (inputLineSplit.length > 1) {
                    try {
                        requestUuid = UUID.fromString(inputLineSplit[0]);
                        inputLine = String.join(":", Arrays.copyOfRange(inputLineSplit, 1, inputLineSplit.length));
                    } catch (IllegalArgumentException e) {
                        // Invalid UUID, treat as no UUID
                    }
                }

                AbstractCommand<?> command = commands.get(inputLine.split(" ")[0].toUpperCase());
                if (command != null) {
                    executeCommandSafely(command, out, requestUuid, dataStore, inputLine);
                } else {
                    out.println(requestUuid, "ERR Unknown Command");
                }

                out.print(">> ");
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeCommandSafely(AbstractCommand<?> command, StorePrintWriter out, UUID requestUuid, InMemoryStore dataStore, String inputLine) {
        try {
            command.executeCommand(out, requestUuid, dataStore, inputLine);
        } catch (AssertionError | Exception e) {
            logger.log(Level.WARNING, e.getMessage());
            out.println(requestUuid, e.getMessage());
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