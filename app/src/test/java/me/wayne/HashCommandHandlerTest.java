package me.wayne;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.wayne.daos.io.StoreBufferedReader;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.StoreMap;

class HashCommandHandlerTest {

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
    void testHgetallCommand() {
        StoreMap storeMap = new StoreMap();
        storeMap.put("field1", "Hello");
        storeMap.put("field2", "World");
        assertEquals("""
                1) field1
                2) Hello
                3) field2
                4) World""", storeMap.getFieldsAndValues().toString());
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
