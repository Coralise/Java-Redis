package me.wayne;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.wayne.daos.io.StoreBufferedReader;
import me.wayne.daos.io.StorePrintWriter;

class SetCommandHandlerTest {

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
    void testSaddCommand() throws IOException {
        String response = sendMessage("SADD set1 member1 member2 member3");
        assertEquals("3", response);
        response = sendMessage("SMEMBERS set1");
        Set<String> expectedMembers = new HashSet<>(Arrays.asList("member1", "member2", "member3"));
        assertEquals(expectedMembers, new HashSet<>(Arrays.asList(response.replace("[", "").replace("]", "").split(", "))));
        response = sendMessage("SADD set1 member2 member4");
        assertEquals("1", response);
    }

    @Test
    void testSremCommand() throws IOException {
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
    void testSinterCommand() throws IOException {
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
    void testSunionCommand() throws IOException {
        sendMessage("SADD sUnion1 a b c d");
        sendMessage("SADD sUnion2 c");
        sendMessage("SADD sUnion3 a c e");
        String response = sendMessage("SUNION sUnion1 sUnion2 sUnion3");
        Set<String> expectedMembers = new HashSet<>(Arrays.asList("a", "b", "c", "d", "e"));
        assertEquals(expectedMembers, new HashSet<>(Arrays.asList(response.replace("[", "").replace("]", "").split(", "))));
    }

    @Test
    void testSdiffCommand() throws IOException {
        sendMessage("SADD sDiff1 a b c d");
        sendMessage("SADD sDiff2 c");
        sendMessage("SADD sDiff3 a c e");
        String response = sendMessage("SDIFF sDiff1 sDiff2 sDiff3");
        Set<String> expectedMembers = new HashSet<>(Arrays.asList("b", "d"));
        assertEquals(expectedMembers, new HashSet<>(Arrays.asList(response.replace("[", "").replace("]", "").split(", "))));
    }

    @Test
    void testSIsMemberCommand() throws IOException {
        sendMessage("SADD myset member1 member2 member3");
        String response = sendMessage("SISMEMBER myset member1");
        assertEquals("1", response);
        response = sendMessage("SISMEMBER myset member4");
        assertEquals("0", response);
    }
}
