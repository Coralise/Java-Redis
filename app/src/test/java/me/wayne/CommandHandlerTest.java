package me.wayne;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

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

    @Test
    public void testJsonSetCommand() throws IOException {
        String response = sendMessage("JSON.SET key5 $ '[\"Deimos\", {\"crashes\": 0}, null]'");
        assertEquals("OK", response);
        response = sendMessage("JSON.GET key5 $");
        assertEquals("[\"Deimos\",{\"crashes\":0},null]", response);
        response = sendMessage("JSON.SET key5 $[anotherKey] '\"Another Key\"'");
        assertEquals("OK", response);
        response = sendMessage("JSON.GET key5 $[anotherKey]");
        assertEquals("Another Key", response);
    }

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
    }
}
