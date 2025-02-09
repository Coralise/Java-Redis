package me.wayne;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListCommandHandlerTest {

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
    void testLpushCommand() throws IOException {
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
    void testRpushCommand() throws IOException {
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
    void testLpopCommand() throws IOException {
        sendMessage("LPUSH list1 value1 value1.2 value1.3");
        String response = sendMessage("LPOP list1");
        assertEquals("[value1.3]", response);
        response = sendMessage("LPOP list1 2");
        assertEquals("[value1.2, value1]", response);
    }

    @Test
    void testRpopCommand() throws IOException {
        sendMessage("RPUSH list2 value1 value1.2 value1.3");
        String response = sendMessage("RPOP list2");
        assertEquals("[value1.3]", response);
        response = sendMessage("RPOP list2 2");
        assertEquals("[value1.2, value1]", response);
    }

    @Test
    void testLrangeCommand() throws IOException {
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
    void testLindexCommand() throws IOException {
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
    void testLsetCommand() throws IOException {
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
}
