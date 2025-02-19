package me.wayne;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.wayne.daos.io.StoreBufferedReader;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.StoreList;

class ListCommandHandlerTest {

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
    void testLpushCommand() {
        StoreList storeList = new StoreList();
        storeList.lPush(List.of("value1", "value1.2", "value1.3"));
        assertEquals("""
                1) value1.3
                2) value1.2
                3) value1""", storeList.range(0, -1).toString());
        storeList.lPush("value2");
        assertEquals("""
                1) value2
                2) value1.3
                3) value1.2
                4) value1""", storeList.range(0, -1).toString());
        storeList.lPush("value3");
        assertEquals("""
                1) value3
                2) value2
                3) value1.3
                4) value1.2
                5) value1""", storeList.range(0, -1).toString());
    }

    @Test
    void testRpushCommand() {
        StoreList storeList = new StoreList();
        storeList.rPush(List.of("value1", "value1.2", "value1.3"));
        assertEquals("""
                1) value1
                2) value1.2
                3) value1.3""", storeList.range(0, -1).toString());
        storeList.rPush("value2");
        assertEquals("""
                1) value1
                2) value1.2
                3) value1.3
                4) value2""", storeList.range(0, -1).toString());
        storeList.rPush("value3");
        assertEquals("""
                1) value1
                2) value1.2
                3) value1.3
                4) value2
                5) value3""", storeList.range(0, -1).toString());
    }

    @Test
    void testLpopCommand() {
        StoreList storeList = new StoreList();
        storeList.lPush(List.of("value1", "value1.2", "value1.3"));
        assertEquals("value1.3", storeList.lPop(1).toString());
        assertEquals("""
                1) value1.2
                2) value1""", storeList.lPop(2).toString());
    }

    @Test
    void testRpopCommand() {
        StoreList storeList = new StoreList();
        storeList.rPush(List.of("value1", "value1.2", "value1.3"));
        assertEquals("value1.3", storeList.rPop(1).toString());
        assertEquals("""
                1) value1.2
                2) value1""", storeList.rPop(2).toString());
    }

    @Test
    void testLrangeCommand() {
        StoreList storeList = new StoreList();
        storeList.rPush(List.of("value1", "value2", "value3"));
        assertEquals("value1", storeList.range(0, 0).toString());
        assertEquals("""
                1) value1
                2) value2
                3) value3""", storeList.range(-3, 2).toString());
        assertEquals("""
                1) value1
                2) value2
                3) value3""", storeList.range(-100, 100).toString());
        assertEquals("[]", storeList.range(5, 10).toString());
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
