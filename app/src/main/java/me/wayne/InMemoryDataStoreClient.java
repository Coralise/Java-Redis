package me.wayne;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class InMemoryDataStoreClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private static final Logger logger = Logger.getLogger(InMemoryDataStoreClient.class.getName());

    public void startConnection(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public String sendMessage(String msg) throws IOException {
        out.println(msg);
        return in.readLine();
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

    public static void main(String[] args) {
        InMemoryDataStoreClient client = new InMemoryDataStoreClient();
        try {
            client.startConnection("127.0.0.1", 6379);
            
            // Test SET command
            logger.info("SET command");
            logger.info(client.sendMessage("SET key1 \"value 1\""));
            
            // Test GET command
            logger.info("GET command");
            logger.info(client.sendMessage("GET key1"));
            
            // Test DELETE command
            logger.info("DELETE command");
            logger.info(client.sendMessage("DELETE key1"));
            
            // Test APPEND command
            logger.info("APPEND command");
            logger.info(client.sendMessage("SET key2 \"value 2\""));
            logger.info(client.sendMessage("APPEND key2 \" appended value 3\""));
            logger.info(client.sendMessage("GET key2"));
            
            // Test STRLEN command
            logger.info("STRLEN command");
            logger.info(client.sendMessage("STRLEN key2"));
            
            // Test INCR command
            logger.info("INCR command");
            logger.info(client.sendMessage("SET key3 1"));
            logger.info(client.sendMessage("INCR key3"));
            logger.info(client.sendMessage("GET key3"));
            
            // Test DECR command
            logger.info("DECR command");
            logger.info(client.sendMessage("DECR key3"));
            logger.info(client.sendMessage("GET key3"));
            
            // Test INCRBY command
            logger.info("INCRBY command");
            logger.info(client.sendMessage("INCRBY key3 5"));
            logger.info(client.sendMessage("GET key3"));
            
            // Test DECRBY command
            logger.info("DECRBY command");
            logger.info(client.sendMessage("DECRBY key3 2"));
            logger.info(client.sendMessage("GET key3"));
            
            // Test GETRANGE command
            logger.info("GETRANGE command");
            logger.info(client.sendMessage("SET key4 value4"));
            logger.info(client.sendMessage("GETRANGE key4 0 2"));
            
            // Test SETRANGE command
            logger.info("SETRANGE command");
            logger.info(client.sendMessage("SETRANGE key4 1 new"));
            logger.info(client.sendMessage("GET key4"));
            
            // Test JSON.SET command
            logger.info("JSON.SET command");
            logger.info(client.sendMessage("JSON.SET key5 $ '[\"Deimos\", {\"crashes\": 0}, null]'"));
            logger.info(client.sendMessage("JSON.GET key5 $"));
            logger.info(client.sendMessage("JSON.SET key5 $[anotherKey] '\"Another Key\"'"));
            logger.info(client.sendMessage("JSON.GET key5 $[anotherKey]"));

            // Test LPUSH command
            logger.info("LPUSH command");
            logger.info(client.sendMessage("LPUSH list1 value1 value1.2 value1.3"));
            logger.info(client.sendMessage("LPUSH list1 value2"));
            logger.info(client.sendMessage("LPUSH list1 value3"));
            logger.info(client.sendMessage("GET list1"));

            // Test RPUSH command
            logger.info("RPUSH command");
            logger.info(client.sendMessage("RPUSH list2 value1 value1.2 value1.3"));
            logger.info(client.sendMessage("RPUSH list2 value2"));
            logger.info(client.sendMessage("RPUSH list2 value3"));
            logger.info(client.sendMessage("GET list2"));

            // Test LPOP command
            logger.info("LPOP command");
            logger.info(client.sendMessage("LPOP list1"));
            logger.info(client.sendMessage("LPOP list1 2"));

            // Test RPOP command
            logger.info("RPOP command");
            logger.info(client.sendMessage("RPOP list2"));
            logger.info(client.sendMessage("RPOP list2 2"));

            // Test LRANGE command
            logger.info("LRANGE command");
            logger.info(client.sendMessage("RPUSH list3 value1"));
            logger.info(client.sendMessage("RPUSH list3 value2"));
            logger.info(client.sendMessage("RPUSH list3 value3"));
            logger.info(client.sendMessage("LRANGE list3 0 0"));
            logger.info(client.sendMessage("LRANGE list3 -3 2"));
            logger.info(client.sendMessage("LRANGE list3 -100 100"));
            logger.info(client.sendMessage("LRANGE list3 5 10"));

            // Test LINDEX command
            logger.info("LINDEX command");
            logger.info(client.sendMessage("RPUSH list4 value1"));
            logger.info(client.sendMessage("RPUSH list4 value2"));
            logger.info(client.sendMessage("LINDEX list4 0"));
            logger.info(client.sendMessage("LINDEX list4 -1"));
            logger.info(client.sendMessage("LINDEX list4 3"));

            // Test LSET command
            logger.info("LSET command");
            logger.info(client.sendMessage("RPUSH list5 value1"));
            logger.info(client.sendMessage("RPUSH list5 value2"));
            logger.info(client.sendMessage("RPUSH list5 value3"));
            logger.info(client.sendMessage("LSET list5 0 \"new Value\""));

            client.stopConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}