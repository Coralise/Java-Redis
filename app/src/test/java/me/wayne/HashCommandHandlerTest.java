package me.wayne;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HashCommandHandlerTest {

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
    void testHsetAndHgetCommands() throws IOException {
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
    void testHgetallCommand() throws IOException {
        sendMessage("HSET myhash field1 \"Hello\" field2 \"World\"");
        String response = sendMessage("HGETALL myhash");
        Set<String> expectedFieldsAndValues = new HashSet<>(Arrays.asList("field1", "Hello", "field2", "World"));
        Set<String> actualFieldsAndValues = new HashSet<>(Arrays.asList(response.replace("[", "").replace("]", "").split(", ")));
        assertEquals(expectedFieldsAndValues, actualFieldsAndValues);
    }

    @Test
    void testHdelCommand() throws IOException {
        sendMessage("HSET myhash field1 \"foo\"");
        String response = sendMessage("HDEL myhash field1");
        assertEquals("1", response);
        response = sendMessage("HDEL myhash field2");
        assertEquals("0", response);
    }

    @Test
    void testHexistsCommand() throws IOException {
        sendMessage("HSET myhash field1 \"foo\"");
        String response = sendMessage("HEXISTS myhash field1");
        assertEquals("1", response);
        response = sendMessage("HEXISTS myhash field2");
        assertEquals("0", response);
    }
}
