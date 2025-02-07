package me.wayne;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CommandHandlerTest {

    Socket socket;
    BufferedReader in = mock(BufferedReader.class);
    PrintWriter out = mock(PrintWriter.class);
                
    @Before
    public void setUp() throws IOException {
        socket = new Socket("127.0.0.1", 3000);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @BeforeClass
    public static void setUpClass() {
        new Thread(App::new).start();
    }

    public String sendMessage(String msg) throws IOException {
        out.println(msg);
        return in.readLine();
    }

    @Test
    public void getArgsTest() {
        String inputLine = "arg1 arg2 \"arg3 arg3 arg3\" arg4 \"arg5 arg5\" arg6";
        CommandHandler commandHandler = new CommandHandler(socket, null);
        List<String> args = commandHandler.getArgs(inputLine);
        assertEquals("arg1", args.get(0));
        assertEquals("arg2", args.get(1));
        assertEquals("arg3 arg3 arg3", args.get(2));
        assertEquals("arg4", args.get(3));
        assertEquals("arg5 arg5", args.get(4));
        assertEquals("arg6", args.get(5));
    }

    @Test
    public void testSetCommand() throws IOException {
        String response = sendMessage("SET key1 \"value 1\"");
        assertEquals("OK", response);

        response = sendMessage("SET key2 \"value 2\" anotherArg");
        assertEquals("ERROR: Invalid number of arguments", response);
        response = sendMessage("SET key2");
        assertEquals("ERROR: Invalid number of arguments", response);
    }

    @Test
    public void testGetCommand() throws IOException {
        sendMessage("SET key1 \"value 1\"");
        String response = sendMessage("GET key1");
        assertEquals("value 1", response);
    }

    @Test
    public void testDeleteCommand() throws IOException {
        sendMessage("SET key1 \"value 1\"");
        String response = sendMessage("DELETE key1");
        assertEquals("value 1", response);
    }

    @Test
    public void testAppendCommand() throws IOException {
        sendMessage("SET key2 \"value 2\"");
        String response = sendMessage("APPEND key2 \" appended value 3\"");
        assertEquals("OK", response);
        response = sendMessage("GET key2");
        assertEquals("value 2 appended value 3", response);
    }

    @Test
    public void testStrlenCommand() throws IOException {
        sendMessage("SET key2 \"value 2\"");
        String response = sendMessage("STRLEN key2");
        assertEquals("7", response);
    }

    @Test
    public void testIncrCommand() throws IOException {
        sendMessage("SET key3 1");
        String response = sendMessage("INCR key3");
        assertEquals("OK", response);
        response = sendMessage("GET key3");
        assertEquals("2", response);
    }

    @Test
    public void testDecrCommand() throws IOException {
        sendMessage("SET key3 1");
        sendMessage("INCR key3");
        String response = sendMessage("DECR key3");
        assertEquals("OK", response);
        response = sendMessage("GET key3");
        assertEquals("1", response);
    }

    @Test
    public void testIncrByCommand() throws IOException {
        sendMessage("SET key3 1");
        String response = sendMessage("INCRBY key3 5");
        assertEquals("OK", response);
        response = sendMessage("GET key3");
        assertEquals("6", response);
    }

    @Test
    public void testDecrByCommand() throws IOException {
        sendMessage("SET key3 1");
        sendMessage("INCRBY key3 5");
        String response = sendMessage("DECRBY key3 2");
        assertEquals("OK", response);
        response = sendMessage("GET key3");
        assertEquals("4", response);
    }

    @Test
    public void testGetRangeCommand() throws IOException {
        sendMessage("SET key4 value4");
        String response = sendMessage("GETRANGE key4 0 2");
        assertEquals("val", response);
    }

    @Test
    public void testSetRangeCommand() throws IOException {
        sendMessage("SET key4 value4");
        String response = sendMessage("SETRANGE key4 1 new");
        assertEquals("vnewe4", response);
        response = sendMessage("GET key4");
        assertEquals("vnewe4", response);
    }

    // @Test
    // public void testJsonSetCommand() throws IOException {
    //     String response = sendMessage("JSON.SET key5 $ '[\"Deimos\", {\"crashes\": 0}, null]'");
    //     assertEquals("OK", response);
    //     response = sendMessage("JSON.GET key5 $");
    //     assertEquals("[\"Deimos\",{\"crashes\":0},null]", response);
    //     response = sendMessage("JSON.SET key5 $[anotherKey] '\"Another Key\"'");
    //     assertEquals("OK", response);
    //     response = sendMessage("JSON.GET key5 $[anotherKey]");
    //     assertEquals("Another Key", response);
    // }

    @Test
    public void testLpushCommand() throws IOException {
        String response = sendMessage("LPUSH list1 value1 value1.2 value1.3");
        assertEquals("3", response);
        response = sendMessage("LPUSH list1 value2");
        assertEquals("4", response);
        response = sendMessage("LPUSH list1 value3");
        assertEquals("5", response);
        response = sendMessage("LRANGE list1 0 -1");
        assertEquals("[value3, value2, value1.3, value1.2, value1]", response);
    }

    @Test
    public void testRpushCommand() throws IOException {
        String response = sendMessage("RPUSH list2 value1 value1.2 value1.3");
        assertEquals("3", response);
        response = sendMessage("RPUSH list2 value2");
        assertEquals("4", response);
        response = sendMessage("RPUSH list2 value3");
        assertEquals("5", response);
        response = sendMessage("LRANGE list2 0 -1");
        assertEquals("[value1, value1.2, value1.3, value2, value3]", response);
    }

    @Test
    public void testLpopCommand() throws IOException {
        sendMessage("LPUSH list1 value1 value1.2 value1.3");
        String response = sendMessage("LPOP list1");
        assertEquals("[value1.3]", response);
        response = sendMessage("LPOP list1 2");
        assertEquals("[value1.2, value1]", response);
    }

    @Test
    public void testRpopCommand() throws IOException {
        sendMessage("RPUSH list2 value1 value1.2 value1.3");
        String response = sendMessage("RPOP list2");
        assertEquals("[value1.3]", response);
        response = sendMessage("RPOP list2 2");
        assertEquals("[value1.2, value1]", response);
    }

    @Test
    public void testLrangeCommand() throws IOException {
        sendMessage("RPUSH list3 value1");
        sendMessage("RPUSH list3 value2");
        sendMessage("RPUSH list3 value3");
        String response = sendMessage("LRANGE list3 0 0");
        assertEquals("[value1]", response);
        response = sendMessage("LRANGE list3 -3 2");
        assertEquals("[value1, value2, value3]", response);
        response = sendMessage("LRANGE list3 -100 100");
        assertEquals("[value1, value2, value3]", response);
        response = sendMessage("LRANGE list3 5 10");
        assertEquals("[]", response);
    }

    @Test
    public void testLindexCommand() throws IOException {
        sendMessage("RPUSH list4 value1");
        sendMessage("RPUSH list4 value2");
        String response = sendMessage("LINDEX list4 0");
        assertEquals("value1", response);
        response = sendMessage("LINDEX list4 -1");
        assertEquals("value2", response);
        response = sendMessage("LINDEX list4 3");
        assertEquals("null", response);
    }

    @Test
    public void testLsetCommand() throws IOException {
        sendMessage("RPUSH list5 value1");
        sendMessage("RPUSH list5 value2");
        sendMessage("RPUSH list5 value3");
        String response = sendMessage("LSET list5 0 \"new Value\"");
        assertEquals("OK", response);
        response = sendMessage("LINDEX list5 0");
        assertEquals("new Value", response);
        response = sendMessage("LSET list5 -1 \"new Value at last\"");
        assertEquals("OK", response);
        response = sendMessage("LINDEX list5 -1");
        assertEquals("new Value at last", response);
    }

    @Test
    public void testSaddCommand() throws IOException {
        String response = sendMessage("SADD set1 member1 member2 member3");
        assertEquals("3", response);
        response = sendMessage("SMEMBERS set1");
        Set<String> expectedMembers = new HashSet<>(Arrays.asList("member1", "member2", "member3"));
        assertEquals(expectedMembers, new HashSet<>(Arrays.asList(response.replace("[", "").replace("]", "").split(", "))));
        response = sendMessage("SADD set1 member2 member4");
        assertEquals("1", response);
    }

    @Test
    public void testSremCommand() throws IOException {
        String response = sendMessage("SADD myset \"one\"");
        assertEquals("1", response);
        response = sendMessage("SADD myset \"two\"");
        assertEquals("1", response);
        response = sendMessage("SADD myset \"three\"");
        assertEquals("1", response);
        response = sendMessage("SREM myset \"one\"");
        assertEquals("1", response);
        response = sendMessage("SREM myset \"four\"");
        assertEquals("0", response);
        response = sendMessage("SMEMBERS myset");
        Set<String> expectedMembers = new HashSet<>(Arrays.asList("two", "three"));
        assertEquals(expectedMembers, new HashSet<>(Arrays.asList(response.replace("[", "").replace("]", "").split(", "))));
    }

    @Test
    public void testSinterCommand() throws IOException {
        String response = sendMessage("SADD sInter1 a b c d");
        assertEquals("4", response);
        response = sendMessage("SADD sInter2 c");
        assertEquals("1", response);
        response = sendMessage("SADD sInter3 a c e");
        assertEquals("3", response);
        response = sendMessage("SINTER sInter1 sInter2 sInter3");
        Set<String> expectedMembers = new HashSet<>(Arrays.asList("c"));
        assertEquals(expectedMembers, new HashSet<>(Arrays.asList(response.replace("[", "").replace("]", "").split(", "))));
    }

    @Test
    public void testSunionCommand() throws IOException {
        sendMessage("SADD sUnion1 a b c d");
        sendMessage("SADD sUnion2 c");
        sendMessage("SADD sUnion3 a c e");
        String response = sendMessage("SUNION sUnion1 sUnion2 sUnion3");
        Set<String> expectedMembers = new HashSet<>(Arrays.asList("a", "b", "c", "d", "e"));
        assertEquals(expectedMembers, new HashSet<>(Arrays.asList(response.replace("[", "").replace("]", "").split(", "))));
    }

    @Test
    public void testSdiffCommand() throws IOException {
        sendMessage("SADD sDiff1 a b c d");
        sendMessage("SADD sDiff2 c");
        sendMessage("SADD sDiff3 a c e");
        String response = sendMessage("SDIFF sDiff1 sDiff2 sDiff3");
        Set<String> expectedMembers = new HashSet<>(Arrays.asList("b", "d"));
        assertEquals(expectedMembers, new HashSet<>(Arrays.asList(response.replace("[", "").replace("]", "").split(", "))));
    }

    @Test
    public void testHsetAndHgetCommands() throws IOException {
        String response = sendMessage("HSET myhash1 field1 \"Hello\"");
        assertEquals("1", response);
        response = sendMessage("HGET myhash1 field1");
        assertEquals("Hello", response);

        response = sendMessage("HSET myhash2 field2 \"Hi\" field3 \"World\"");
        assertEquals("2", response);
        response = sendMessage("HGET myhash2 field2");
        assertEquals("Hi", response);
        response = sendMessage("HGET myhash2 field3");
        assertEquals("World", response);
    }

    @Test
    public void testHgetallCommand() throws IOException {
        sendMessage("HSET myhash field1 \"Hello\" field2 \"World\"");
        String response = sendMessage("HGETALL myhash");
        Set<String> expectedFieldsAndValues = new HashSet<>(Arrays.asList("field1", "Hello", "field2", "World"));
        Set<String> actualFieldsAndValues = new HashSet<>(Arrays.asList(response.replace("[", "").replace("]", "").split(", ")));
        assertEquals(expectedFieldsAndValues, actualFieldsAndValues);
    }

    @Test
    public void testHdelCommand() throws IOException {
        sendMessage("HSET myhash field1 \"foo\"");
        String response = sendMessage("HDEL myhash field1");
        assertEquals("1", response);
        response = sendMessage("HDEL myhash field2");
        assertEquals("0", response);
    }

    @Test
    public void testHexistsCommand() throws IOException {
        sendMessage("HSET myhash field1 \"foo\"");
        String response = sendMessage("HEXISTS myhash field1");
        assertEquals("1", response);
        response = sendMessage("HEXISTS myhash field2");
        assertEquals("0", response);
    }

    @Test
    public void testZaddCommand() throws IOException {
        String response = sendMessage("ZADD zset1 1 member1");
        assertEquals("1", response);
        response = sendMessage("ZADD zset1 3 member3");
        assertEquals("1", response);
        response = sendMessage("ZADD zset1 2 member2");
        assertEquals("1", response);
        response = sendMessage("ZADD zset1 2 member2");
        assertEquals("0", response); // member2 already exists with the same score
        response = sendMessage("ZRANGE zset1 0 -1 WITHSCORES");
        assertEquals("[member1, 1, member2, 2, member3, 3]", response);
    }

    @Test
    public void testZaddCommandWithNxOption() throws IOException {
        String response = sendMessage("ZADD zset2 NX 1 member1");
        assertEquals("1", response);
        response = sendMessage("ZADD zset2 CH NX 2 member1");
        assertEquals("0", response); // member1 already exists, NX prevents update
        response = sendMessage("ZRANGE zset2 0 -1 WITHSCORES");
        assertEquals("[member1, 1]", response);
    }

    @Test
    public void testZaddCommandWithXxOption() throws IOException {
        String response = sendMessage("ZADD zset3 XX 1 member1");
        assertEquals("0", response); // member1 does not exist, XX prevents addition
        response = sendMessage("ZADD zset3 1 member1");
        assertEquals("1", response);
        response = sendMessage("ZADD zset3 CH XX 2 member1");
        assertEquals("1", response); // member1 exists, XX allows update
        response = sendMessage("ZRANGE zset3 0 -1 WITHSCORES");
        assertEquals("[member1, 2]", response);
    }

    @Test
    public void testZaddCommandWithChOption() throws IOException {
        String response = sendMessage("ZADD zset4 CH 1 member1");
        assertEquals("1", response);
        response = sendMessage("ZADD zset4 CH 2 member1");
        assertEquals("1", response); // CH counts the update as a change
        response = sendMessage("ZRANGE zset4 0 -1 WITHSCORES");
        assertEquals("[member1, 2]", response);
    }

    @Test
    public void testZaddCommandWithIncrOption() throws IOException {
        String response = sendMessage("ZADD zset5 INCR 1 member1");
        assertEquals("1", response);
        response = sendMessage("ZADD zset5 CH INCR 2 member1");
        assertEquals("1", response);
        response = sendMessage("ZRANGE zset5 0 -1 WITHSCORES");
        assertEquals("[member1, 3]", response);
    }

    @Test
    public void testZrankCommand() throws IOException {
        sendMessage("ZADD zset6 1 member1");
        sendMessage("ZADD zset6 2 member2");
        sendMessage("ZADD zset6 3 member3");
        String response = sendMessage("ZRANK zset6 member1");
        assertEquals("0", response);
        response = sendMessage("ZRANK zset6 member2");
        assertEquals("1", response);
        response = sendMessage("ZRANK zset6 member3");
        assertEquals("2", response);
        response = sendMessage("ZRANK zset6 member3 WITHSCORE");
        assertEquals("[2, 3]", response);
        response = sendMessage("ZRANK zset6 member4");
        assertEquals("null", response);
    }

    @Test
    public void testZremCommand() throws IOException {
        sendMessage("ZADD zset7 1 member1");
        sendMessage("ZADD zset7 2 member2");
        sendMessage("ZADD zset7 3 member3");
        String response = sendMessage("ZREM zset7 member2 member1 member5");
        assertEquals("2", response);
        response = sendMessage("ZRANGE zset7 0 -1 WITHSCORES");
        assertEquals("[member3, 3]", response);
        response = sendMessage("ZREM zset7 member4");
        assertEquals("0", response);
    }

    @Test
    public void testZrangeByScoreCommand() throws IOException {
        sendMessage("ZADD zset8 1 member1");
        sendMessage("ZADD zset8 2 member2");
        sendMessage("ZADD zset8 3 member3");
        String response = sendMessage("ZRANGE zset8 1 2 BYSCORE");
        assertEquals("[member1, member2]", response);
        response = sendMessage("ZRANGE zset8 2 3 BYSCORE WITHSCORES");
        assertEquals("[member2, 2, member3, 3]", response);
        response = sendMessage("ZRANGE zset8 0 1 BYSCORE");
        assertEquals("[member1]", response);
        response = sendMessage("ZRANGE zset8 4 5 BYSCORE");
        assertEquals("[]", response);
    }

    @Test
    public void testXaddAndXrangeComplexCommand() throws IOException {
        String id1 = sendMessage("XADD mystream * field1 value1");
        wait(1);
        String id2 = sendMessage("XADD mystream * field2 value2");
        wait(1);
        String id3 = sendMessage("XADD mystream * field3 value3");
        wait(1);
        String id4 = sendMessage("XADD mystream * field4 value4");

        String response = sendMessage("XRANGE mystream - + COUNT 2");
        assertEquals(2, response.split("],").length);

        response = sendMessage("XRANGE mystream - +");
        assertEquals(4, response.split("],").length);

        response = sendMessage("XRANGE mystream - + COUNT 1");
        assertEquals(1, response.split("],").length);

        response = sendMessage("XRANGE mystream " + id1 + "-0 " + id2 + "-0");
        assertEquals("[[" + id1 + ", [field1, value1]], [" + id2 + ", [field2, value2]]]", response);

        response = sendMessage("XRANGE mystream " + id3 + "-0 " + id4 + "-0");
        assertEquals("[[" + id3 + ", [field3, value3]], [" + id4 + ", [field4, value4]]]", response);
        
        response = sendMessage("XRANGE mystream " + id2 + "-0 " + id4 + "-0 COUNT 2");
        assertEquals("[[" + id2 + ", [field2, value2]], [" + id3 + ", [field3, value3]]]", response);
        assertEquals(2, response.split("],").length);

        // Interchange between -, +, IDs, and IDs not in the stream
        response = sendMessage("XRANGE mystream - " + id2 + "-0");
        assertEquals("[[" + id1 + ", [field1, value1]], [" + id2 + ", [field2, value2]]]", response);

        response = sendMessage("XRANGE mystream " + id3 + "-0 +");
        assertEquals("[[" + id3 + ", [field3, value3]], [" + id4 + ", [field4, value4]]]", response);

        response = sendMessage("XRANGE mystream 0-0 " + id2 + "-0");
        assertEquals("[[" + id1 + ", [field1, value1]], [" + id2 + ", [field2, value2]]]", response);

        response = sendMessage("XRANGE mystream " + id3 + "-0 9999999999999-0");
        assertEquals("[[" + id3 + ", [field3, value3]], [" + id4 + ", [field4, value4]]]", response);

        response = sendMessage("XRANGE mystream 0-0 9999999999999-0");
        assertEquals(4, response.split("],").length);

        // Using IDs with (
        response = sendMessage("XRANGE mystream (" + id1 + "-0 " + id3 + "-0");
        assertEquals("[[" + id2 + ", [field2, value2]], [" + id3 + ", [field3, value3]]]", response);

        response = sendMessage("XRANGE mystream " + id1 + "-0 (" + id4 + "-0");
        assertEquals("[[" + id1 + ", [field1, value1]], [" + id2 + ", [field2, value2]], [" + id3 + ", [field3, value3]]]", response);
    }

    @Test
    public void testXreadCommand() throws IOException {
        String id1 = sendMessage("XADD mystreamXread * field1 value1");
        wait(1);
        String id2 = sendMessage("XADD mystreamXread * field2 value2");
        wait(1);
        String id3 = sendMessage("XADD mystreamXread * field3 value3");
        wait(1);
        String id4 = sendMessage("XADD mystreamXread * field4 value4");

        String response = sendMessage("XREAD COUNT 2 STREAMS mystreamXread 0-0");
        assertEquals("{mystreamXread=[[" + id1 + ", [field1, value1]], [" + id2 + ", [field2, value2]]]}", response);

        response = sendMessage("XREAD COUNT 1 STREAMS mystreamXread " + id1);
        assertEquals("{mystreamXread=[[" + id2 + ", [field2, value2]]]}", response);

        response = sendMessage("XREAD STREAMS mystreamXread " + id2);
        assertEquals("{mystreamXread=[[" + id3 + ", [field3, value3]], [" + id4 + ", [field4, value4]]]}", response);

        response = sendMessage("XREAD STREAMS mystreamXread " + id3);
        assertEquals("{mystreamXread=[[" + id4 + ", [field4, value4]]]}", response);

        response = sendMessage("XREAD STREAMS mystreamXread " + id4);
        assertEquals("{mystreamXread=[]}", response);

        // Additional tests with multiple streams
        wait(1);
        String id5 = sendMessage("XADD mystream2 * fieldA valueA");
        wait(1);
        String id6 = sendMessage("XADD mystream2 * fieldB valueB");

        response = sendMessage("XREAD COUNT 2 STREAMS mystreamXread mystream2 0-0 0-0");
        assertEquals("{mystreamXread=[[" + id1 + ", [field1, value1]], [" + id2 + ", [field2, value2]]], mystream2=[[" + id5 + ", [fieldA, valueA]], [" + id6 + ", [fieldB, valueB]]]}",
            response);

        response = sendMessage("XREAD STREAMS mystreamXread mystream2 " + id2 + " 0-0");
        assertEquals("{mystreamXread=[[" + id3 + ", [field3, value3]], [" + id4 + ", [field4, value4]]], mystream2=[[" + id5 + ", [fieldA, valueA]], [" + id6 + ", [fieldB, valueB]]]}",
            response);
    }

    // @Test
    // public void testXReadBlockOption() throws IOException {
    //     // Add initial entries to the stream
    //     sendMessage("XADD mystream * field1 value1");
    //     wait(1);
    //     String id2 = sendMessage("XADD mystream * field2 value2");

    //     // Start a thread to add a new entry after a delay
    //     new Thread(() -> {
    //         System.out.println("Adding new entry after 2 seconds");
    //         try {
    //             Thread.sleep(2000); // Wait for 2000 milliseconds
    //             sendMessage("XADD mystream * field3 value3");
    //         } catch (InterruptedException | IOException e) {
    //             Thread.currentThread().interrupt();
    //         }
    //     }).start();

    //     // Call xRead with BLOCK option and a timeout of 5000 milliseconds
    //     long blockTimeout = 10000;
    //     String response = sendMessage("XREAD BLOCK " + blockTimeout + " STREAMS mystream " + id2);

    //     assertEquals(1, response.split("],").length);
    // }

    @Test
    public void testSIsMemberCommand() throws IOException {
        sendMessage("SADD myset member1 member2 member3");
        String response = sendMessage("SISMEMBER myset member1");
        assertEquals("1", response);
        response = sendMessage("SISMEMBER myset member4");
        assertEquals("0", response);
    }

    @SuppressWarnings("squid:S2925")
    private void wait(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}