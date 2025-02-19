package me.wayne;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.wayne.daos.io.StoreBufferedReader;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.PrintableList;
import me.wayne.daos.storevalues.StoreSet;

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
    void testSaddCommand() {
        StoreSet storeSet = new StoreSet();
        storeSet.add("member1");
        storeSet.add("member2");
        storeSet.add("member3");
        assertEquals(3, storeSet.size());
        assertEquals("""
                1) member1
                2) member2
                3) member3""", new PrintableList<>(storeSet).toString());
        storeSet.add("member2");
        storeSet.add("member4");
        assertEquals("""
                1) member1
                2) member2
                3) member3
                4) member4""", new PrintableList<>(storeSet).toString());
    }

    @Test
    void testSremCommand() {
        StoreSet storeSet = new StoreSet();
        storeSet.add("one");
        storeSet.add("two");
        storeSet.add("three");
        assertEquals(3, storeSet.size());
        storeSet.remove("one");
        assertEquals(2, storeSet.size());
        storeSet.remove("four");
        assertEquals(2, storeSet.size());
        assertEquals("""
                1) two
                2) three""", new PrintableList<>(storeSet).toString());
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
    void testSunionCommand() {
        StoreSet set1 = new StoreSet();
        StoreSet set2 = new StoreSet();
        StoreSet set3 = new StoreSet();
        set1.addAll(List.of("a", "b", "c", "d"));
        set2.add("c");
        set3.addAll(List.of("a", "c", "e"));
        StoreSet unionSet = StoreSet.union(set1, set2, set3);
        assertEquals("""
                1) a
                2) b
                3) c
                4) d
                5) e""", new PrintableList<>(unionSet).toString());
    }

    @Test
    void testSdiffCommand() {
        StoreSet set1 = new StoreSet();
        StoreSet set2 = new StoreSet();
        StoreSet set3 = new StoreSet();
        set1.addAll(List.of("a", "b", "c", "d"));
        set2.add("c");
        set3.addAll(List.of("a", "c", "e"));
        StoreSet diffSet = StoreSet.difference(set1, set2, set3);
        assertEquals("""
                1) b
                2) d""", new PrintableList<>(diffSet).toString());
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
