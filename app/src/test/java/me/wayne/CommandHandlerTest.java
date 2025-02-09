package me.wayne;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandHandlerTest {

    Socket socket;
    BufferedReader in = mock(BufferedReader.class);
    PrintWriter out = mock(PrintWriter.class);
                
    @BeforeEach
    void setUp() throws IOException {
        socket = new Socket("127.0.0.1", 3000);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @BeforeAll
    static void setUpClass() {
        new Thread(App::new).start();
    }

    String sendMessage(String msg) throws IOException {
        out.println(msg);
        return in.readLine();
    }

    @Test
    void getArgsTest() {
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
    void testSetCommand() throws IOException {
        String response = sendMessage("SET key1 \"value 1\"");
        assertEquals("OK", response);

        response = sendMessage("SET key2 \"value 2\" anotherArg");
        assertEquals("Unknown option: ANOTHERARG", response);
        response = sendMessage("SET key2");
        assertEquals("ERROR: Invalid number of arguments", response);
    }

    @Test
    void testGetCommand() throws IOException {
        sendMessage("SET key1 \"value 1\"");
        String response = sendMessage("GET key1");
        assertEquals("value 1", response);
    }

    @Test
    void testDeleteCommand() throws IOException {
        sendMessage("SET key1 \"value 1\"");
        String response = sendMessage("DELETE key1");
        assertEquals("value 1", response);
    }

    @Test
    void testAppendCommand() throws IOException {
        sendMessage("SET key2 \"value 2\"");
        String response = sendMessage("APPEND key2 \" appended value 3\"");
        assertEquals("OK", response);
        response = sendMessage("GET key2");
        assertEquals("value 2 appended value 3", response);
    }

    @Test
    void testStrlenCommand() throws IOException {
        sendMessage("SET key2 \"value 2\"");
        String response = sendMessage("STRLEN key2");
        assertEquals("7", response);
    }

    @Test
    void testIncrCommand() throws IOException {
        sendMessage("SET key3 1");
        String response = sendMessage("INCR key3");
        assertEquals("OK", response);
        response = sendMessage("GET key3");
        assertEquals("2", response);
    }

    @Test
    void testDecrCommand() throws IOException {
        sendMessage("SET key3 1");
        sendMessage("INCR key3");
        String response = sendMessage("DECR key3");
        assertEquals("OK", response);
        response = sendMessage("GET key3");
        assertEquals("1", response);
    }

    @Test
    void testIncrByCommand() throws IOException {
        sendMessage("SET key3 1");
        String response = sendMessage("INCRBY key3 5");
        assertEquals("OK", response);
        response = sendMessage("GET key3");
        assertEquals("6", response);
    }

    @Test
    void testDecrByCommand() throws IOException {
        sendMessage("SET key3 1");
        sendMessage("INCRBY key3 5");
        String response = sendMessage("DECRBY key3 2");
        assertEquals("OK", response);
        response = sendMessage("GET key3");
        assertEquals("4", response);
    }

    @Test
    void testGetRangeCommand() throws IOException {
        sendMessage("SET key4 value4");
        String response = sendMessage("GETRANGE key4 0 2");
        assertEquals("val", response);
    }

    @Test
    void testSetRangeCommand() throws IOException {
        sendMessage("SET key4 value4");
        String response = sendMessage("SETRANGE key4 1 new");
        assertEquals("vnewe4", response);
        response = sendMessage("GET key4");
        assertEquals("vnewe4", response);
    }

    @Test
    void testZaddCommand() throws IOException {
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
    void testZaddCommandWithNxOption() throws IOException {
        String response = sendMessage("ZADD zset2 NX 1 member1");
        assertEquals("1", response);
        response = sendMessage("ZADD zset2 CH NX 2 member1");
        assertEquals("0", response); // member1 already exists, NX prevents update
        response = sendMessage("ZRANGE zset2 0 -1 WITHSCORES");
        assertEquals("[member1, 1]", response);
    }

    @Test
    void testZaddCommandWithXxOption() throws IOException {
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
    void testZaddCommandWithChOption() throws IOException {
        String response = sendMessage("ZADD zset4 CH 1 member1");
        assertEquals("1", response);
        response = sendMessage("ZADD zset4 CH 2 member1");
        assertEquals("1", response); // CH counts the update as a change
        response = sendMessage("ZRANGE zset4 0 -1 WITHSCORES");
        assertEquals("[member1, 2]", response);
    }

    @Test
    void testZaddCommandWithIncrOption() throws IOException {
        String response = sendMessage("ZADD zset5 INCR 1 member1");
        assertEquals("1", response);
        response = sendMessage("ZADD zset5 CH INCR 2 member1");
        assertEquals("1", response);
        response = sendMessage("ZRANGE zset5 0 -1 WITHSCORES");
        assertEquals("[member1, 3]", response);
    }

    @Test
    void testZrankCommand() throws IOException {
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
    void testZremCommand() throws IOException {
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
    void testZrangeByScoreCommand() throws IOException {
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
    void testXaddAndXrangeComplexCommand() throws IOException {
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
    void testXreadCommand() throws IOException {
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

    @Test
    void testGeoAddCommand() throws IOException {
        String response = sendMessage("GEOADD geosetGeoAdd 13.361389 38.115556 Palermo");
        assertEquals("1", response);
        response = sendMessage("GEOADD geosetGeoAdd 15.087269 37.502669 Catania");
        assertEquals("1", response);
        response = sendMessage("GEOADD geosetGeoAdd NX CH 13.361389 38.115556 Palermo");
        assertEquals("0", response); // Palermo already exists
        response = sendMessage("GEOADD geosetGeoAdd XX CH 15.361389 48.115556 Foo");
        assertEquals("0", response); // Foo doesn't exist but XX option is used
        response = sendMessage("GEOADD geosetGeoAdd CH 13.361389 38.115556 Palermo");
        assertEquals("1", response); // Palermo already exists, CH counts the update as a change
        response = sendMessage("GEOADD geosetGeoAdd CH 16.361389 39.115556 Palermo");
        assertEquals("1", response); // Palermo updated, CH counts the update as a change
    }

    @Test
    void testGeoAddMultipleMembers() throws IOException {
        String response = sendMessage("GEOADD geoset 13.361389 38.115556 Palermo 15.087269 37.502669 Catania");
        assertEquals("2", response);
        response = sendMessage("GEOADD geoset NX 13.361389 38.115556 Palermo 16.361389 39.115556 Rome");
        assertEquals("1", response); // Only Rome is added, Palermo already exists
        response = sendMessage("GEOADD geoset 0 0 Center");
        assertEquals("1", response);
    }

    @Test
    void testGeoSearchCommand() throws IOException {
        String response = sendMessage("GEOADD Sicily 13.361389 38.115556 \"Palermo\" 15.087269 37.502669 \"Catania\"");
        assertEquals("2", response);
        sendMessage("GEOADD Sicily 12.758489 38.788135 \"edge1\" 17.241510 38.788135 \"edge2\"");

        response = sendMessage("GEOSEARCH Sicily FROMLONLAT 15 37 BYRADIUS 200 km ASC");
        assertEquals("[[Catania], [Palermo]]", response);

        response = sendMessage("GEOSEARCH Sicily FROMLONLAT 15 37 BYBOX 400 400 km ASC WITHCOORD WITHDIST");
        assertEquals("[[Catania, 56.42541980224439, [15.087269, 37.502669]], [Palermo, 190.38870648178343, [13.361389, 38.115556]], [edge2, 279.6614460135385, [17.24151, 38.788135]], [edge1, 279.66150770904176, [12.758489, 38.788135]]]", response);

        response = sendMessage("GEOSEARCH Sicily FROMLONLAT 15 37 BYBOX 400 400 km ASC WITHCOORD WITHDIST WITHHASH");
        assertEquals("[[Catania, 56.42541980224439, 3476216502357864, [15.087269, 37.502669]], [Palermo, 190.38870648178343, 3476004292229755, [13.361389, 38.115556]], [edge2, 279.6614460135385, 3478108620044552, [17.24151, 38.788135]], [edge1, 279.66150770904176, 3476038982646536, [12.758489, 38.788135]]]", response);

        response = sendMessage("GEOSEARCH Sicily FROMMEMBER Palermo BYRADIUS 200 km");
        assertEquals("[[Palermo], [edge1], [Catania]]", response);

        response = sendMessage("GEOSEARCH Sicily FROMMEMBER Palermo BYRADIUS 200 km WITHDIST");
        assertEquals("[[Palermo, 0.0], [edge1, 91.3748784790091], [Catania, 166.2273571807551]]", response);

        response = sendMessage("GEOSEARCH Sicily FROMMEMBER Palermo BYRADIUS 200 km WITHDIST WITHHASH");
        assertEquals("[[Palermo, 0.0, 3476004292229755], [edge1, 91.3748784790091, 3476038982646536], [Catania, 166.2273571807551, 3476216502357864]]", response);

        response = sendMessage("GEOSEARCH Sicily FROMMEMBER Palermo BYRADIUS 200 km WITHDIST WITHHASH DESC");
        assertEquals("[[Catania, 166.2273571807551, 3476216502357864], [edge1, 91.3748784790091, 3476038982646536], [Palermo, 0.0, 3476004292229755]]", response);
    }

    @Test
    void testGeoDistCommand() throws IOException {
        sendMessage("GEOADD geosetGeoDist 13.361389 38.115556 Palermo");
        sendMessage("GEOADD geosetGeoDist 15.087269 37.502669 Catania");
        String response = sendMessage("GEODIST geosetGeoDist Palermo Catania km");
        assertEquals("166.2273571807551", response);
        response = sendMessage("GEODIST geosetGeoDist Palermo Catania m");
        assertEquals("166227.3571807551", response);
        response = sendMessage("GEODIST geosetGeoDist Palermo Catania mi");
        assertEquals("103.28914783747071", response);
        response = sendMessage("GEODIST geosetGeoDist Palermo Catania ft");
        assertEquals("545365.3450812175", response);
    }

    @Test
    void testBitCommands() throws IOException {
        sendMessage("SETBIT bitmapsarestrings 2 1");
        sendMessage("SETBIT bitmapsarestrings 3 1");
        sendMessage("SETBIT bitmapsarestrings 5 1");
        sendMessage("SETBIT bitmapsarestrings 10 1");
        sendMessage("SETBIT bitmapsarestrings 11 1");
        sendMessage("SETBIT bitmapsarestrings 14 1");
        String response = sendMessage("GET bitmapsarestrings");
        assertEquals("42", response);

        // Add GETBIT commands
        assertEquals("1", sendMessage("GETBIT bitmapsarestrings 2"));
        assertEquals("1", sendMessage("GETBIT bitmapsarestrings 3"));
        assertEquals("1", sendMessage("GETBIT bitmapsarestrings 5"));
        assertEquals("0", sendMessage("GETBIT bitmapsarestrings 200"));
        assertEquals("1", sendMessage("GETBIT bitmapsarestrings 10"));
        assertEquals("1", sendMessage("GETBIT bitmapsarestrings 11"));
        assertEquals("1", sendMessage("GETBIT bitmapsarestrings 14"));
        assertEquals("0", sendMessage("GETBIT bitmapsarestrings 1"));
        assertEquals("0", sendMessage("GETBIT bitmapsarestrings 4"));

        // Add BITCOUNT commands
        response = sendMessage("BITCOUNT bitmapsarestrings");
        assertEquals("6", response);
        response = sendMessage("BITCOUNT bitmapsarestrings 0 0");
        assertEquals("3", response);
        response = sendMessage("BITCOUNT bitmapsarestrings 0 1 BYTE");
        assertEquals("6", response);
        response = sendMessage("BITCOUNT bitmapsarestrings 0 10 BIT");
        assertEquals("4", response);
        response = sendMessage("BITCOUNT bitmapsarestrings 0 13 BIT");
        assertEquals("5", response);

        sendMessage("SETBIT bitsetTest 1 1");
        sendMessage("SETBIT bitsetTest 3 1");
        sendMessage("SETBIT bitsetTest 5 1");
        sendMessage("SETBIT bitsetTest 9 1");
        sendMessage("SETBIT bitsetTest 10 1");
        sendMessage("SETBIT bitsetTest 13 1");
        sendMessage("SETBIT bitsetTest 15 1");
        sendMessage("SETBIT bitsetTest 17 1");
        sendMessage("SETBIT bitsetTest 18 1");
        sendMessage("SETBIT bitsetTest 19 1");
        sendMessage("SETBIT bitsetTest 22 1");
        sendMessage("SETBIT bitsetTest 23 1");
        sendMessage("SETBIT bitsetTest 25 1");
        sendMessage("SETBIT bitsetTest 26 1");
        sendMessage("SETBIT bitsetTest 27 1");
        sendMessage("SETBIT bitsetTest 29 1");

        // Get the bitset as a string and verify the result
        response = sendMessage("GET bitsetTest");
        assertEquals("Test", response);

        // Add BITCOUNT commands for bitsetTest
        response = sendMessage("BITCOUNT bitsetTest");
        assertEquals("16", response);

        response = sendMessage("BITCOUNT bitsetTest 0 1");
        assertEquals("7", response);

        response = sendMessage("BITCOUNT bitsetTest 0 2 BYTE");
        assertEquals("12", response);

        response = sendMessage("BITCOUNT bitsetTest 0 16 BIT");
        assertEquals("7", response);

        response = sendMessage("BITCOUNT bitsetTest 0 100 BIT");
        assertEquals("16", response);

        response = sendMessage("BITCOUNT bitsetTest 1 100");
        assertEquals("13", response);

        sendMessage("SET stringTest stringTest");
        sendMessage("SETBIT stringTest 20 1");
        response = sendMessage("GET stringTest");
        assertEquals("stzingTest", response);
    }

    @Test
    void testBitOpCommand() throws IOException {
        sendMessage("SET key1 \"foobar\"");
        sendMessage("SET key2 \"abcdef\"");
        String response = sendMessage("BITOP AND dest key1 key2");
        assertEquals("6", response);
        response = sendMessage("GET dest");
        assertEquals("`bc`ab", response);

        sendMessage("SET key3 \"ghijklmnop\"");
        response = sendMessage("BITOP OR dest key1 key2 key3");
        assertEquals("10", response);
        response = sendMessage("GET dest");
        assertEquals("goono~mnop", response);
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