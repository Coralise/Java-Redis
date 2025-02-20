package me.wayne.daos;

import java.util.HashMap;
import java.util.Map;

import me.wayne.daos.commands.*;

public abstract class Commands {

    private static final Map<String, AbstractCommand<?>> commandMap = new HashMap<>();
    static {
        commandMap.put("APPEND", new AppendCommand());
        commandMap.put("DECRBY", new DecrByCommand());
        commandMap.put("DECR", new DecrCommand());
        commandMap.put("DELETE", new DeleteCommand());
        commandMap.put("GET", new GetCommand());
        commandMap.put("GETRANGE", new GetRangeCommand());
        commandMap.put("HDEL", new HDelCommand());
        commandMap.put("HEXISTS", new HExistsCommand());
        commandMap.put("HGETALL", new HGetAllCommand());
        commandMap.put("HGET", new HGetCommand());
        commandMap.put("HSET", new HSetCommand());
        commandMap.put("INCRBY", new IncrByCommand());
        commandMap.put("INCR", new IncrCommand());
        commandMap.put("LINDEX", new LIndexCommand());
        commandMap.put("LPOP", new LPopCommand());
        commandMap.put("LPUSH", new LPushCommand());
        commandMap.put("LRANGE", new LRangeCommand());
        commandMap.put("LSET", new LSetCommand());
        commandMap.put("RPOP", new RPopCommand());
        commandMap.put("RPUSH", new RPushCommand());
        commandMap.put("SADD", new SAddCommand());
        commandMap.put("SDIFF", new SDiffCommand());
        commandMap.put("SET", new SetCommand());
        commandMap.put("SETRANGE", new SetRangeCommand());
        commandMap.put("SINTER", new SInterCommand());
        commandMap.put("SISMEMBER", new SIsMemberCommand());
        commandMap.put("SMEMBERS", new SMembersCommand());
        commandMap.put("SREM", new SRemCommand());
        commandMap.put("STRLEN", new StrLenCommand());
        commandMap.put("SUNION", new SUnionCommand());
        commandMap.put("XADD", new XAddCommand());
        commandMap.put("XRANGE", new XRangeCommand());
        commandMap.put("XREAD", new XReadCommand());
        commandMap.put("ZADD", new ZAddCommand());
        commandMap.put("ZRANGE", new ZRangeCommand());
        commandMap.put("ZRANK", new ZRankCommand());
        commandMap.put("ZREM", new ZRemCommand());
        commandMap.put("XGROUP", new XGroupCommand());
        commandMap.put("XREADGROUP", new XReadGroupCommand());
        commandMap.put("XACK", new XAckCommand());
        commandMap.put("GEOADD", new GeoAddCommand());
        commandMap.put("GEOSEARCH", new GeoSearchCommand());
        commandMap.put("GEODIST", new GeoDistCommand());
        commandMap.put("SETBIT", new SetBitCommand());
        commandMap.put("GETBIT", new GetBitCommand());
        commandMap.put("BITCOUNT", new BitCountCommand());
        commandMap.put("BITOP", new BitOpCommand());
        commandMap.put("EXISTS", new ExistsCommand());
        commandMap.put("PFADD", new PfAddCommand());
        commandMap.put("PFCOUNT", new PfCountCommand());
        commandMap.put("PFMERGE", new PfMergeCommand());
        commandMap.put("TS.CREATE", new TsCreateCommand());
        commandMap.put("TS.ADD", new TsAddCommand());
        commandMap.put("TS.RANGE", new TsRangeCommand());
        commandMap.put("TS.GET", new TsGetCommand());
        commandMap.put("JSON.SET", new JsonSetCommand());
        commandMap.put("JSON.GET", new JsonGetCommand());
        commandMap.put("JSON.DEL", new JsonDelCommand());
        commandMap.put("JSON.ARRAPPEND", new JsonArrAppendCommand());
        commandMap.put("BITFIELD", new BitFieldCommand());
        commandMap.put("EXPIRE", new ExpireCommand());
        commandMap.put("EXPIREAT", new ExpireAtCommand());
        commandMap.put("TTL", new TtlCommand());
        commandMap.put("MULTI", new MultiCommand());
        commandMap.put("SUBSCRIBE", new SubscribeCommand());
        commandMap.put("UNSUBSCRIBE", new UnsubscribeCommand());
        commandMap.put("PUBLISH", new PublishCommand());
        commandMap.put("SAVE", new SaveCommand());
        commandMap.put("VADD", new VAddCommand());
        commandMap.put("VCREATE", new VCreateCommand());
        commandMap.put("VSEARCH.COSIM", new VSearchCoSimCommand());
        commandMap.put("VSEARCH.EUCLIDIST", new VSearchEucliDistCommand());
    }

    private Commands() {}

    @SuppressWarnings("unchecked")
    public static <T> AbstractCommand<T> getCommand(String command) {
        return (AbstractCommand<T>) commandMap.get(command.toUpperCase());
    }
    
}
