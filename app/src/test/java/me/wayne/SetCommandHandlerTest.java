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
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SetCommandHandlerTest {

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
    public void testSIsMemberCommand() throws IOException {
        sendMessage("SADD myset member1 member2 member3");
        String response = sendMessage("SISMEMBER myset member1");
        assertEquals("1", response);
        response = sendMessage("SISMEMBER myset member4");
        assertEquals("0", response);
    }
}
