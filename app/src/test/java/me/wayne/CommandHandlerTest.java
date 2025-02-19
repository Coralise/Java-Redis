package me.wayne;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.wayne.daos.io.StoreBufferedReader;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.GeoMember;
import me.wayne.daos.storevalues.GeoSpace;
import me.wayne.daos.storevalues.ScoreMember;
import me.wayne.daos.storevalues.StoreSortedSet;
import me.wayne.daos.storevalues.timeseries.DuplicatePolicy;
import me.wayne.daos.storevalues.timeseries.TimeSeries;
import me.wayne.daos.storevalues.timeseries.TimeSeriesAggregation;

class CommandHandlerTest {

    Socket socket;
    StoreBufferedReader in = mock(StoreBufferedReader.class);
    StorePrintWriter out = mock(StorePrintWriter.class);
                
    @BeforeEach
    void setUp() throws IOException {
        socket = new Socket("127.0.0.1", 3000);
        out = new StorePrintWriter(socket.getOutputStream());
        in = new StoreBufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @BeforeAll
    static void setUpClass() {
        new Thread(App::new).start();
    }

    String sendMessage(String msg) throws IOException {
        UUID requestUuid = out.sendCommand(msg);
        return in.waitResponse(requestUuid);
    }

    @Test
    void getArgsTest() {
        String inputLine = "arg1 arg2 \"arg3 arg3 arg3\" arg4 \"arg5 arg5\" arg6";
        CommandHandler commandHandler = new CommandHandler(socket);
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
        assertEquals("ERR Invalid number of arguments", response);
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
    void testZaddCommand() {
        StoreSortedSet treeSet = new StoreSortedSet();
        treeSet.add(new ScoreMember(1, "member1"));
        treeSet.add(new ScoreMember(3, "member3"));
        treeSet.add(new ScoreMember(2, "member2"));
        treeSet.add(new ScoreMember(2, "member2"));
        assertEquals(3, treeSet.size());
        assertEquals("""
                    1) member1
                    2) 1
                    3) member2
                    4) 2
                    5) member3
                    6) 3""", treeSet.range(0, -1, true, false, false).toString());
    }

    @Test
    void testZaddCommandWithNxOption() {
        StoreSortedSet treeSet = new StoreSortedSet();
        treeSet.add(true, false, false, false, false, false, new ScoreMember(1, "member1"));
        assertEquals(1, treeSet.size());
        treeSet.add(true, false, false, false, true, false, new ScoreMember(1, "member1"));
        assertEquals(1, treeSet.size());
        assertEquals("""
                1) member1
                2) 1""", treeSet.range(0, -1, true, false, false).toString());
    }

    @Test
    void testZaddCommandWithXxOption() {
        StoreSortedSet treeSet = new StoreSortedSet();
        treeSet.add(false, true, false, false, false, false, new ScoreMember(1, "member1"));
        assertEquals(0, treeSet.size());
        treeSet.add(false, false, false, false, false, false, new ScoreMember(1, "member1"));
        assertEquals(1, treeSet.size());
        treeSet.add(false, true, false, false, true, false, new ScoreMember(2, "member1"));
        assertEquals("""
                1) member1
                2) 2""", treeSet.range(0, -1, true, false, false).toString());
    }

    @Test
    void testZaddCommandWithChOption() {
        StoreSortedSet treeSet = new StoreSortedSet();
        treeSet.add(false, false, true, false, false, false, new ScoreMember(1, "member1"));
        assertEquals(1, treeSet.size());
        treeSet.add(false, false, true, false, false, false, new ScoreMember(2, "member1"));
        assertEquals(1, treeSet.size());
        assertEquals("""
                1) member1
                2) 2""", treeSet.range(0, -1, true, false, false).toString());
    }

    @Test
    void testZaddCommandWithIncrOption() {
        StoreSortedSet treeSet = new StoreSortedSet();
        treeSet.add(false, false, false, true, false, false, new ScoreMember(1, "member1"));
        assertEquals(1, treeSet.size());
        treeSet.add(false, false, false, false, true, true, new ScoreMember(2, "member1"));
        assertEquals(1, treeSet.size());
        assertEquals("""
                1) member1
                2) 3""", treeSet.range(0, -1, true, false, false).toString());
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
    void testZremCommand() {
        StoreSortedSet treeSet = new StoreSortedSet();
        treeSet.add(new ScoreMember(1, "member1"));
        treeSet.add(new ScoreMember(2, "member2"));
        treeSet.add(new ScoreMember(3, "member3"));
        assertEquals(3, treeSet.size());
        treeSet.remove(new ScoreMember("member2"));
        treeSet.remove(new ScoreMember("member1"));
        assertEquals(1, treeSet.size());
        assertEquals("""
                1) member3
                2) 3""", treeSet.range(0, -1, true, false, false).toString());
    }

    @Test
    void testZrangeByScoreCommand() {
        StoreSortedSet treeSet = new StoreSortedSet();
        treeSet.add(new ScoreMember(1, "member1"));
        treeSet.add(new ScoreMember(2, "member2"));
        treeSet.add(new ScoreMember(3, "member3"));
        assertEquals(3, treeSet.size());
        assertEquals("""
                1) member1
                2) member2""", treeSet.range(0, -1, false, false, true).toString());
        assertEquals("""
                1) member2
                2) 2
                3) member3
                4) 3""", treeSet.range(2, 3, true, false, true).toString());
        assertEquals("""
                member1""", treeSet.range(0, 1, false, false, true).toString());
        assertEquals("[]", treeSet.range(4, 5, false, false, true).toString());
    }

    // @Test
    // void testXaddAndXrangeComplexCommand() throws IOException {
    //     String id1 = sendMessage("XADD mystream 1000 field1 value1");
    //     String id2 = sendMessage("XADD mystream 1000 field2 value2");
    //     String id3 = sendMessage("XADD mystream 2000 field3 value3");
    //     String id4 = sendMessage("XADD mystream 3000 field4 value4");
    //     String id5 = sendMessage("XADD mystream 4000 field5 value5");

    //     String response = sendMessage("XRANGE mystream - + COUNT 3");
    //     assertEquals("[[1000-0, [field1, value1]], [1000-1, [field2, value2]], [2000-0, [field3, value3]]]", response);

    //     response = sendMessage("XRANGE mystream - +");
    //     assertEquals(5, response.split("],").length);

    //     response = sendMessage("XRANGE mystream - + COUNT 1");
    //     assertEquals(1, response.split("],").length);

    //     response = sendMessage("XRANGE mystream " + id1 + " " + id2);
    //     assertEquals("[[" + id1 + ", [field1, value1]], [" + id2 + ", [field2, value2]]]", response);

    //     response = sendMessage("XRANGE mystream " + id3 + " " + id4);
    //     assertEquals("[[" + id3 + ", [field3, value3]], [" + id4 + ", [field4, value4]]]", response);
        
    //     response = sendMessage("XRANGE mystream " + id2 + " " + id4 + " COUNT 2");
    //     assertEquals("[[" + id2 + ", [field2, value2]], [" + id3 + ", [field3, value3]]]", response);
    //     assertEquals(2, response.split("],").length);

    //     // Interchange between -, +, IDs, and IDs not in the stream
    //     response = sendMessage("XRANGE mystream - " + id2);
    //     assertEquals("[[" + id1 + ", [field1, value1]], [" + id2 + ", [field2, value2]]]", response);

    //     response = sendMessage("XRANGE mystream " + id3 + " +");
    //     assertEquals("[[" + id3 + ", [field3, value3]], [" + id4 + ", [field4, value4]], [" + id5 + ", [field5, value5]]]", response);

    //     response = sendMessage("XRANGE mystream 0-0 " + id2);
    //     assertEquals("[[" + id1 + ", [field1, value1]], [" + id2 + ", [field2, value2]]]", response);

    //     response = sendMessage("XRANGE mystream " + id3 + " 9999999999999-0");
    //     assertEquals("[[" + id3 + ", [field3, value3]], [" + id4 + ", [field4, value4]], [" + id5 + ", [field5, value5]]]", response);

    //     response = sendMessage("XRANGE mystream 0-0 9999999999999-0");
    //     assertEquals(5, response.split("],").length);

    //     // Using IDs with (
    //     response = sendMessage("XRANGE mystream (" + id1 + " " + id3);
    //     assertEquals("[[" + id2 + ", [field2, value2]], [" + id3 + ", [field3, value3]]]", response);

    //     response = sendMessage("XRANGE mystream " + id1 + " (" + id4);
    //     assertEquals("[[" + id1 + ", [field1, value1]], [" + id2 + ", [field2, value2]], [" + id3 + ", [field3, value3]]]", response);
    // }

    // @Test
    // void testXreadCommand() throws IOException {
    //     String id1 = sendMessage("XADD mystreamXread * field1 value1");
    //     wait(1);
    //     String id2 = sendMessage("XADD mystreamXread * field2 value2");
    //     wait(1);
    //     String id3 = sendMessage("XADD mystreamXread * field3 value3");
    //     wait(1);
    //     String id4 = sendMessage("XADD mystreamXread * field4 value4");

    //     String response = sendMessage("XREAD COUNT 2 STREAMS mystreamXread 0-0");
    //     assertEquals("{mystreamXread=[[" + id1 + ", [field1, value1]], [" + id2 + ", [field2, value2]]]}", response);

    //     response = sendMessage("XREAD COUNT 1 STREAMS mystreamXread " + id1);
    //     assertEquals("{mystreamXread=[[" + id2 + ", [field2, value2]]]}", response);

    //     response = sendMessage("XREAD STREAMS mystreamXread " + id2);
    //     assertEquals("{mystreamXread=[[" + id3 + ", [field3, value3]], [" + id4 + ", [field4, value4]]]}", response);

    //     response = sendMessage("XREAD STREAMS mystreamXread " + id3);
    //     assertEquals("{mystreamXread=[[" + id4 + ", [field4, value4]]]}", response);

    //     response = sendMessage("XREAD STREAMS mystreamXread " + id4);
    //     assertEquals("{mystreamXread=[]}", response);

    //     // Additional tests with multiple streams
    //     wait(1);
    //     String id5 = sendMessage("XADD mystream2 * fieldA valueA");
    //     wait(1);
    //     String id6 = sendMessage("XADD mystream2 * fieldB valueB");

    //     response = sendMessage("XREAD COUNT 2 STREAMS mystreamXread mystream2 0-0 0-0");
    //     assertEquals("{mystreamXread=[[" + id1 + ", [field1, value1]], [" + id2 + ", [field2, value2]]], mystream2=[[" + id5 + ", [fieldA, valueA]], [" + id6 + ", [fieldB, valueB]]]}",
    //         response);

    //     response = sendMessage("XREAD STREAMS mystreamXread mystream2 " + id2 + " 0-0");
    //     assertEquals("{mystreamXread=[[" + id3 + ", [field3, value3]], [" + id4 + ", [field4, value4]]], mystream2=[[" + id5 + ", [fieldA, valueA]], [" + id6 + ", [fieldB, valueB]]]}",
    //         response);
    // }

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
    void testGeoSearchCommand() {

        GeoSpace geoSpace = new GeoSpace();
        geoSpace.add(new GeoMember(13.361389, 38.115556, "Palermo"));
        geoSpace.add(new GeoMember(15.087269, 37.502669, "Catania"));
        assertEquals(2, geoSpace.size());
        geoSpace.add(new GeoMember(12.758489, 38.788135, "edge1"));
        geoSpace.add(new GeoMember(17.241510, 38.788135, "edge2"));

        assertEquals("""
                1) Catania
                2) Palermo""", geoSpace.geoSearch(null, "15", "37", "200", "km", null, null, "asc", null, false, false, false, false).toString());

        // response = sendMessage("GEOSEARCH Sicily FROMLONLAT 15 37 BYBOX 400 400 km ASC WITHCOORD WITHDIST");
        // assertEquals("[[Catania, 56.42541980224439, [15.087269, 37.502669]], [Palermo, 190.38870648178343, [13.361389, 38.115556]], [edge2, 279.6614460135385, [17.24151, 38.788135]], [edge1, 279.66150770904176, [12.758489, 38.788135]]]", response);

        // response = sendMessage("GEOSEARCH Sicily FROMLONLAT 15 37 BYBOX 400 400 km ASC WITHCOORD WITHDIST WITHHASH");
        // assertEquals("[[Catania, 56.42541980224439, 3476216502357864, [15.087269, 37.502669]], [Palermo, 190.38870648178343, 3476004292229755, [13.361389, 38.115556]], [edge2, 279.6614460135385, 3478108620044552, [17.24151, 38.788135]], [edge1, 279.66150770904176, 3476038982646536, [12.758489, 38.788135]]]", response);

        // response = sendMessage("GEOSEARCH Sicily FROMMEMBER Palermo BYRADIUS 200 km");
        // assertEquals("[[Palermo], [edge1], [Catania]]", response);

        // response = sendMessage("GEOSEARCH Sicily FROMMEMBER Palermo BYRADIUS 200 km WITHDIST");
        // assertEquals("[[Palermo, 0.0], [edge1, 91.3748784790091], [Catania, 166.2273571807551]]", response);

        // response = sendMessage("GEOSEARCH Sicily FROMMEMBER Palermo BYRADIUS 200 km WITHDIST WITHHASH");
        // assertEquals("[[Palermo, 0.0, 3476004292229755], [edge1, 91.3748784790091, 3476038982646536], [Catania, 166.2273571807551, 3476216502357864]]", response);

        // response = sendMessage("GEOSEARCH Sicily FROMMEMBER Palermo BYRADIUS 200 km WITHDIST WITHHASH DESC");
        // assertEquals("[[Catania, 166.2273571807551, 3476216502357864], [edge1, 91.3748784790091, 3476038982646536], [Palermo, 0.0, 3476004292229755]]", response);
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

    @Test
    void testPfAddCommand() throws IOException {
        String response = sendMessage("PFADD hll a b c d e f g");
        assertEquals("1", response);
        response = sendMessage("PFADD hll h i j k l m n o p q r s t u v w x y z");
        assertEquals("1", response);
        response = sendMessage("PFADD hll a b c");
        assertEquals("0", response); // Elements already exist
    }

    @Test
    void testPfCountCommand() throws IOException {
        sendMessage("PFADD hll1 a b c d e f g");
        String response = sendMessage("PFCOUNT hll1");
        assertEquals("7", response);
        sendMessage("PFADD hll1 h i j k l m n o p q r s t u v w x y z");
        response = sendMessage("PFCOUNT hll1");
        assertEquals("26", response);
    }

    @Test
    void testPfMergeCommand() throws IOException {
        sendMessage("PFADD hll1 a b c d e f g");
        sendMessage("PFADD hll2 h i j k l m n o p q r s t u v w x y z");
        String response = sendMessage("PFMERGE hllMerged hll1 hll2");
        assertEquals("OK", response);
        response = sendMessage("PFCOUNT hllMerged");
        assertEquals("26", response);
    }

    @Test
    void testPfComplexCommands() throws IOException {
        sendMessage("PFADD hll1 a b c d e f g");
        sendMessage("PFADD hll2 h i j k l m n o p q r s t u v w x y z");
        sendMessage("PFADD hll3 1 2 3 4 5 6 7 8 9 10");
        String response = sendMessage("PFMERGE hllMerged hll1 hll2 hll3");
        assertEquals("OK", response);
        response = sendMessage("PFCOUNT hllMerged");
        assertEquals("36", response);
    }

    @Test
    void testTsCreateCommand() throws IOException {
        String response = sendMessage("TS.CREATE temperature:2:32 RETENTION 60000 DUPLICATE_POLICY MAX LABELS sensor_id 2 area_id 32");
        assertEquals("OK", response);
    }

    @Test
    void testTsAddCommand() throws IOException {
        String response = sendMessage("TS.ADD temperature:3:11 1548149183000 27 RETENTION 31536000000 DUPLICATE_POLICY LAST");
        assertEquals("1548149183000", response);
        response = sendMessage("TS.ADD temperature:3:11 1000 30");
        assertEquals("ERROR: Timestamp is past the retention time: 1000", response);
        long timestamp = System.currentTimeMillis();
        response = sendMessage("TS.ADD temperature:3:11 * 32");
        assertEquals((double) timestamp, Double.parseDouble(response), 1000);
    }

    @Test
    void testTsRangeCommand() {
        TimeSeries ts = new TimeSeries("temperature:4:22", DuplicatePolicy.MAX, 60000, Map.of("sensor_id", "4", "area_id", "22"), 0, 0);
        ts.add(1548149183000L, 27);
        ts.add(1548149184000L, 28);
        ts.add(1548149185000L, 29);
        ts.add(1548149186000L, 30);

        assertEquals("""
                1) 1) 1548149183000
                   2) 27.0
                2) 1) 1548149184000
                   2) 28.0
                3) 1) 1548149185000
                   2) 29.0
                4) 1) 1548149186000
                   2) 30.0""", ts.range(1548149183000L, 1548149186000L, null, null, 0, null, null, 0, null, false).toString());

        assertEquals("""
                        1) 1) 1548149183000
                           2) 27.5
                        2) 1) 1548149185000
                           2) 29.5""", ts.range(1548149183000L, 1548149186000L, null, null, 0, null, TimeSeriesAggregation.AVG, 2000, null, false).toString());

        assertEquals("""
                        1) 1) 1548149183000
                           2) 55.0
                        2) 1) 1548149185000
                           2) 59.0""", ts.range(1548149183000L, 1548149186000L, null, null, 0, null, TimeSeriesAggregation.SUM, 2000, null, false).toString());

        assertEquals("""
                        1) 1) 1548149183000
                           2) 27.0
                        2) 1) 1548149185000
                           2) 29.0""", ts.range(1548149183000L, 1548149186000L, null, null, 0, null, TimeSeriesAggregation.MIN, 2000, null, false).toString());

        assertEquals("""
                        1) 1) 1548149183000
                           2) 28.0
                        2) 1) 1548149185000
                           2) 30.0""", ts.range(1548149183000L, 1548149186000L, null, null, 0, null, TimeSeriesAggregation.MAX, 2000, null, false).toString());

        assertEquals("""
                        1) 1) 1548149183000
                           2) 2.0
                        2) 1) 1548149185000
                           2) 2.0""", ts.range(1548149183000L, 1548149186000L, null, null, 0, null, TimeSeriesAggregation.COUNT, 2000, null, false).toString());
        
        assertEquals("""
                        1) 1) 1548149179000
                           2) 0.0
                        2) 1) 1548149181000
                           2) 0.0
                        3) 1) 1548149183000
                           2) 2.0
                        4) 1) 1548149185000
                           2) 2.0""", ts.range(1548149183000L, 1548149186000L, null, null, 0, "1548149179000", TimeSeriesAggregation.COUNT, 2000, null, true).toString());

        assertEquals("""
                        1) 1) 1548149183000
                           2) 27.5
                        2) 1) 1548149185000
                           2) 29.5""", ts.range(1548149183000L, 1548149186000L, null, null, 0, null, TimeSeriesAggregation.AVG, 2000, null, false).toString());

        assertEquals("""
                        1) 1) 1548149182000
                           2) 27.0
                        2) 1) 1548149184000
                           2) 28.5
                        3) 1) 1548149186000
                           2) 30.0""", ts.range(1548149183000L, 1548149186000L, null, null, 0, "END", TimeSeriesAggregation.AVG, 2000, null, false).toString());

        assertEquals("""
                        1) 1548149186000
                        2) 30.0""", ts.get().toString());
    }

    @Test
    void testJsonSetCommand() throws IOException {
        String response = sendMessage("JSON.SET doc $ \"{\\\"a\\\":2, \\\"b\\\":3, \\\"nested\\\":{\\\"a\\\":4, \\\"b\\\":null}}\"");
        assertEquals("OK", response);
    }

    @Test
    void testJsonGetCommand() throws IOException {
        sendMessage("JSON.SET doc $ \"{\\\"a\\\":2, \\\"b\\\":3, \\\"nested\\\":{\\\"a\\\":4, \\\"b\\\":null}}\"");
        String response = sendMessage("JSON.GET doc $..b");
        assertEquals("[\"3\",\"null\"]", response);
        response = sendMessage("JSON.GET doc $..a $..b");
        assertEquals("{$..a=[\"2\",\"4\"], $..b=[\"3\",\"null\"]}", response);
    }

    @Test
    void testJsonDelCommand() throws IOException {
        sendMessage("JSON.SET doc $ \"{\\\"a\\\":1, \\\"nested\\\":{\\\"a\\\":2, \\\"b\\\":3}}\"");
        String response = sendMessage("JSON.DEL doc $..a");
        assertEquals("2", response);
        response = sendMessage("JSON.GET doc $");
        assertEquals("{\"nested\":{\"b\":3}}", response);
    }

    @Test
    void testJsonArrAppendCommand() throws IOException {
        sendMessage("JSON.SET item:1 $ \"{\\\"name\\\":\\\"Noise-cancelling Bluetooth headphones\\\",\\\"description\\\":\\\"Wireless Bluetooth headphones with noise-cancelling technology\\\",\\\"connection\\\":{\\\"wireless\\\":true,\\\"type\\\":\\\"Bluetooth\\\"},\\\"price\\\":99.98,\\\"stock\\\":25,\\\"colors\\\":[\\\"black\\\",\\\"silver\\\"]}\"");
        String response = sendMessage("JSON.ARRAPPEND item:1 $.colors \"blue\" red");
        assertEquals("4", response);
        response = sendMessage("JSON.GET item:1 $");
        assertEquals("{\"name\":\"Noise-cancelling Bluetooth headphones\",\"description\":\"Wireless Bluetooth headphones with noise-cancelling technology\",\"connection\":{\"wireless\":true,\"type\":\"Bluetooth\"},\"price\":99.98,\"stock\":25,\"colors\":[\"black\",\"silver\",\"blue\",\"red\"]}", response);
    }

    @Test
    void testBitFieldCommandPart1() throws IOException {
        String response;

        sendMessage("BITFIELD bf SET i4 13 13");
        response = sendMessage("BITFIELD bf GET i4 13");
        assertEquals("[-3]", response);

        sendMessage("BITFIELD bf SET i4 12 5");
        response = sendMessage("BITFIELD bf GET i4 12");
        assertEquals("[5]", response);

        response = sendMessage("BITFIELD bf GET i4 13");
        assertEquals("[-5]", response);

        response = sendMessage("BITFIELD bf GET i4 15");
        assertEquals("[-4]", response);

        response = sendMessage("BITFIELD bf GET i8 12");
        assertEquals("[88]", response);

        sendMessage("BITFIELD bf OVERFLOW WRAP SET i4 0 20");
        response = sendMessage("BITFIELD bf GET i4 0");
        assertEquals("[4]", response);

        sendMessage("BITFIELD bf OVERFLOW SAT SET i4 0 20");
        response = sendMessage("BITFIELD bf GET i4 0");
        assertEquals("[7]", response);

        sendMessage("BITFIELD bf OVERFLOW FAIL SET i4 0 20");
        response = sendMessage("BITFIELD bf GET i4 0");
        assertEquals("[7]", response);

        sendMessage("BITFIELD bf OVERFLOW SAT SET i4 0 7");
        response = sendMessage("BITFIELD bf GET i4 0");
        assertEquals("[7]", response);

        sendMessage("BITFIELD bf OVERFLOW FAIL SET i4 0 8");
        response = sendMessage("BITFIELD bf GET i4 0");
        assertEquals("[7]", response);

        sendMessage("BITFIELD bf OVERFLOW FAIL SET u4 0 7");
        response = sendMessage("BITFIELD bf GET u4 0");
        assertEquals("[7]", response);

        sendMessage("BITFIELD bf OVERFLOW FAIL SET u4 0 15");
        response = sendMessage("BITFIELD bf GET u4 0");
        assertEquals("[15]", response);

        sendMessage("BITFIELD bf OVERFLOW FAIL SET u4 0 20");
        response = sendMessage("BITFIELD bf GET u4 0");
        assertEquals("[15]", response);
    }

    @Test
    void testBitFieldCommandPart2() throws IOException {
        String response;

        sendMessage("BITFIELD bf2 OVERFLOW FAIL SET u4 0 7");
        response = sendMessage("BITFIELD bf2 GET u4 0");
        assertEquals("[7]", response);

        sendMessage("BITFIELD bf2 OVERFLOW FAIL SET u4 0 15");
        response = sendMessage("BITFIELD bf2 GET u4 0");
        assertEquals("[15]", response);

        sendMessage("BITFIELD bf2 OVERFLOW FAIL SET u4 0 16");
        response = sendMessage("BITFIELD bf2 GET u4 0");
        assertEquals("[15]", response);

        // Additional assertions
        sendMessage("BITFIELD bf2 OVERFLOW SAT SET i4 0 -8");
        response = sendMessage("BITFIELD bf2 GET i4 0");
        assertEquals("[-8]", response);

        sendMessage("BITFIELD bf2 OVERFLOW SAT INCRBY i4 0 -1");
        response = sendMessage("BITFIELD bf2 GET i4 0");
        assertEquals("[-8]", response);

        sendMessage("BITFIELD bf2 OVERFLOW FAIL SET i4 0 -8");
        response = sendMessage("BITFIELD bf2 GET i4 0");
        assertEquals("[-8]", response);

        sendMessage("BITFIELD bf2 OVERFLOW FAIL INCRBY i4 0 -1");
        response = sendMessage("BITFIELD bf2 GET i4 0");
        assertEquals("[-8]", response);

        // Test multiple subcommands
        sendMessage("BITFIELD mykey INCRBY u2 100 1 OVERFLOW SAT INCRBY u2 102 1");
        response = sendMessage("BITFIELD mykey GET u2 100");
        assertEquals("[1]", response);
        response = sendMessage("BITFIELD mykey GET u2 102");
        assertEquals("[1]", response);

        sendMessage("BITFIELD mykey INCRBY u2 100 1 OVERFLOW SAT INCRBY u2 102 1");
        response = sendMessage("BITFIELD mykey GET u2 100");
        assertEquals("[2]", response);
        response = sendMessage("BITFIELD mykey GET u2 102");
        assertEquals("[2]", response);

        sendMessage("BITFIELD mykey INCRBY u2 100 1 OVERFLOW SAT INCRBY u2 102 1");
        response = sendMessage("BITFIELD mykey GET u2 100");
        assertEquals("[3]", response);
        response = sendMessage("BITFIELD mykey GET u2 102");
        assertEquals("[3]", response);

        sendMessage("BITFIELD mykey INCRBY u2 100 1 OVERFLOW SAT INCRBY u2 102 1");
        response = sendMessage("BITFIELD mykey GET u2 100");
        assertEquals("[0]", response);
        response = sendMessage("BITFIELD mykey GET u2 102");
        assertEquals("[3]", response);
    }

    @Test
    void testExpireCommand() throws IOException {
        sendMessage("SET key1 \"value 1\"");
        String response = sendMessage("EXPIRE key1 1");
        assertEquals("1", response);
        response = sendMessage("GET key1");
        assertEquals("value 1", response);
        wait(2);
        response = sendMessage("GET key1");
        assertEquals("null", response);

        sendMessage("SET key2 \"value 2\"");
        response = sendMessage("EXPIRE key2 5");
        assertEquals("1", response);
        response = sendMessage("GET key2");
        assertEquals("value 2", response);
        wait(3);
        response = sendMessage("GET key2");
        assertEquals("value 2", response);
        wait(3);
        response = sendMessage("GET key2");
        assertEquals("null", response);

        sendMessage("SET key3 \"value 3\"");
        response = sendMessage("EXPIRE key3 0");
        assertEquals("1", response);
        response = sendMessage("GET key3");
        assertEquals("null", response);
    }

    @Test
    void testExpireJson() throws IOException {
        sendMessage("JSON.SET doc $ \"{\\\"a\\\":1, \\\"b\\\":2}\"");
        String response = sendMessage("EXPIRE doc 1");
        assertEquals("1", response);
        wait(2);
        response = sendMessage("GET doc");
        assertEquals("null", response);

        sendMessage("JSON.SET doc $ \"{\\\"a\\\":1, \\\"b\\\":2}\"");
        sendMessage("EXPIRE doc 5");
        sendMessage("JSON.SET doc $ \"{\\\"a\\\":3, \\\"b\\\":4}\"");
        response = sendMessage("TTL doc");
        assertEquals("-1", response);
    }

    @Test
    void testExpireHashTable() throws IOException {
        sendMessage("HSET myhash field1 \"Hello\"");
        String response = sendMessage("EXPIRE myhash 1");
        assertEquals("1", response);
        response = sendMessage("HGET myhash field1");
        assertEquals("Hello", response);
        wait(2);
        response = sendMessage("HGET myhash field1");
        assertEquals("null", response);

        sendMessage("HSET myhash field1 \"Hello\"");
        sendMessage("EXPIRE myhash 5");
        sendMessage("HSET myhash field1 \"World\"");
        response = sendMessage("TTL myhash");
        assertEquals("-1", response); // Expiration should be removed
    }

    @Test
    void testExpireSet() throws IOException {
        sendMessage("SADD mysetExpire member1");
        String response = sendMessage("EXPIRE mysetExpire 1");
        assertEquals("1", response);
        response = sendMessage("SMEMBERS mysetExpire");
        assertEquals("member1", response);
        wait(2);
        response = sendMessage("SMEMBERS mysetExpire");
        assertEquals("[]", response);

        sendMessage("SADD mysetExpire member1");
        sendMessage("EXPIRE mysetExpire 5");
        sendMessage("SADD mysetExpire member2");
        response = sendMessage("TTL mysetExpire");
        assertEquals("-1", response);

        sendMessage("SREM mysetExpire member1");
        response = sendMessage("TTL mysetExpire");
        assertEquals("-1", response);

        sendMessage("SADD mysetExpire member3");
        response = sendMessage("TTL mysetExpire");
        assertEquals("-1", response);
    }

    @Test
    void testMultiExecTransaction() throws IOException {
        sendMessage("MULTI");
        sendMessage("SET key1 \"value 1\"");
        sendMessage("SET key2 \"value 2\"");
        String response = sendMessage("EXEC");
        assertEquals("[OK, OK]", response);

        response = sendMessage("GET key1");
        assertEquals("value 1", response);

        response = sendMessage("GET key2");
        assertEquals("value 2", response);
    }

    @Test
    void testMultiExecTransactionWithError() throws IOException {
        sendMessage("MULTI");
        sendMessage("SET key1 \"value 1\"");
        sendMessage("INVALIDCOMMAND"); // Command ignored
        sendMessage("SET key2 Invalid Command");
        sendMessage("SET key3 \"value 3\"");
        sendMessage("SET key4 Invalid 2");
        String response = sendMessage("EXEC");
        assertEquals("[OK, -Unknown option: COMMAND, OK, -Unknown option: 2]", response);
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