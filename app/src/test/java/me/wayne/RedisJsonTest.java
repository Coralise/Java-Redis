package me.wayne;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.JsonNode;

import me.wayne.daos.RedisJson;

class RedisJsonTest {
    
    @Test
    void redisJsonTest() {
        RedisJson redisJson = new RedisJson();

        // Set root ($)
        redisJson.set("$", "{\"users\": [\"Alice\", \"Bob\"]}", false, false);

        // Set object values
        redisJson.set("$.a", "2", false, false);   // Add new field
        redisJson.set("$.b", "8", false, false);   // Add another field
        assertThrows(RuntimeException.class, () -> redisJson.set("$.x.y", "5", false, false)); // Fail to add nested field (x does not exist)

        // Set array values
        redisJson.set("$.users[0]", "Charlie", false, false); // Modify "Alice" -> "Charlie"
        redisJson.set("$.users[2]", "Eve", false, false);     // Expands array and adds "Eve"

        // Conditional operations
        boolean updated = redisJson.set("$.z", "10", true, false); // XX (should fail)
        boolean added = redisJson.set("$.z", "10", false, true);   // NX (should succeed)

        // Print JSON
        System.out.println(redisJson.getJson());
        assertEquals("{\"users\":[\"Charlie\",\"Bob\",\"Eve\"],\"a\":2,\"b\":8,\"z\":10}", redisJson.getJson());
        assertFalse(updated);
        assertTrue(added);
    }

    @Test
    void complexJsonTest() {
        RedisJson redisJson = new RedisJson();

        // Set root ($)
        redisJson.set("$", "{\"users\": [{\"name\": \"Alice\", \"age\": 30}, {\"name\": \"Bob\", \"age\": 25}], \"active\": true}", false, false);

        // Modify nested object values
        redisJson.set("$.users[0].age", "31", false, false); // Update Alice's age
        redisJson.set("$.users[1].name", "\"Robert\"", false, false); // Update Bob's name

        // Add new nested object
        redisJson.set("$.users[1].address", "{\"city\": \"New York\", \"zip\": \"10001\"}", false, false);

        // Add new array element with nested object
        redisJson.set("$.users[2]", "{\"name\": \"Charlie\", \"age\": 28}", false, false);

        // Conditional operations
        boolean updated = redisJson.set("$.active", "false", true, false); // XX (should succeed)
        boolean added = redisJson.set("$.newField", "true", false, true);  // NX (should succeed)

        // Print JSON
        System.out.println(redisJson.getJson());
        assertEquals("{\"users\":[{\"name\":\"Alice\",\"age\":31},{\"name\":\"Robert\",\"age\":25,\"address\":{\"city\":\"New York\",\"zip\":\"10001\"}},{\"name\":\"Charlie\",\"age\":28}],\"active\":false,\"newField\":true}", redisJson.getJson());
        assertTrue(updated);
        assertTrue(added);
    }

    @Test
    void jsonMultiTest() {
        RedisJson redisJson = new RedisJson();

        // Set root ($)
        redisJson.set("$", "{\"users\": [{\"name\": \"Alice\", \"age\": 30}, {\"name\": \"Bob\", \"age\": 25}], \"active\": true, \"multiline\": {\"a\": {\"a\": \"Value1\"}, \"b\": {\"a\": \"Value1\"}, \"c\": {\"a\": \"Value1\"}}}", false, false);

        redisJson.set("$.multiline..a", "New Value", false, false);

        assertEquals("{\"users\":[{\"name\":\"Alice\",\"age\":30},{\"name\":\"Bob\",\"age\":25}],\"active\":true,\"multiline\":{\"a\":\"New Value\",\"b\":{\"a\":\"New Value\"},\"c\":{\"a\":\"New Value\"}}}", redisJson.getJson());
    }

    @Test
    void getChildNodeTest() {
        RedisJson redisJson = new RedisJson();

        // Set root ($)
        redisJson.set("$", "{\"users\": [{\"name\": \"Alice\", \"age\": 30}, {\"name\": \"Bob\", \"age\": 25}], \"active\": true}", false, false);

        // Get child node
        JsonNode user0Name = redisJson.getChildNode("$.users[0].name");
        JsonNode user1Age = redisJson.getChildNode("$.users[1].age");
        JsonNode activeStatus = redisJson.getChildNode("$.active");

        // Assertions
        assertEquals("\"Alice\"", user0Name.toString());
        assertEquals("25", user1Age.toString());
        assertEquals("true", activeStatus.toString());
    }

    @Test
    void getChildNodeNestedTest() {
        RedisJson redisJson = new RedisJson();

        // Set root ($)
        redisJson.set("$", "{\"users\": [{\"name\": \"Alice\", \"age\": 30, \"address\": {\"city\": \"Wonderland\"}}, {\"name\": \"Bob\", \"age\": 25, \"address\": {\"city\": \"Builderland\"}}], \"active\": true}", false, false);

        // Get nested child node
        JsonNode user0City = redisJson.getChildNode("$.users[0].address.city");
        JsonNode user1City = redisJson.getChildNode("$.users[1].address.city");

        // Assertions
        assertEquals("\"Wonderland\"", user0City.toString());
        assertEquals("\"Builderland\"", user1City.toString());
    }

    @Test
    void nestedArraysTest() {
        RedisJson redisJson = new RedisJson();

        // Set root ($)
        redisJson.set("$", "{\"matrix\": [[1, 2, 3], [4, 5, 6], [7, 8, 9]]}", false, false);

        // Modify nested array values
        redisJson.set("$.matrix[0][0]", "10", false, false); // Update 1 -> 10
        redisJson.set("$.matrix[1][1]", "50", false, false); // Update 5 -> 50
        redisJson.set("$.matrix[2][2]", "90", false, false); // Update 9 -> 90

        // Add new nested array element
        redisJson.set("$.matrix[3]", "[10, 11, 12]", false, false);

        // Get nested array values
        JsonNode matrix00 = redisJson.getChildNode("$.matrix[0][0]");
        JsonNode matrix11 = redisJson.getChildNode("$.matrix[1][1]");
        JsonNode matrix22 = redisJson.getChildNode("$.matrix[2][2]");
        JsonNode matrix3 = redisJson.getChildNode("$.matrix[3]");

        // Assertions
        assertEquals("10", matrix00.toString());
        assertEquals("50", matrix11.toString());
        assertEquals("90", matrix22.toString());
        assertEquals("[10,11,12]", matrix3.toString());
    }

    @Test
    void getNonExistentPathTest() {
        RedisJson redisJson = new RedisJson();

        // Set root ($)
        redisJson.set("$", "{\"users\": [{\"name\": \"Alice\", \"age\": 30}, {\"name\": \"Bob\", \"age\": 25}], \"active\": true}", false, false);

        // Get non-existent path
        assertThrows(IllegalArgumentException.class, () -> redisJson.getChildNode("$.users[2].name"));
        assertThrows(IllegalArgumentException.class, () -> redisJson.getChildNode("$.users[1].address.city"));
    }

    @Test
    void getMultiPathTest() {
        RedisJson redisJson = new RedisJson();

        // Set root ($)
        redisJson.set("$", "{\"users\": [{\"name\": \"Alice\", \"age\": 30}, {\"name\": \"Bob\", \"age\": 25}], \"active\": true, \"multiline\": {\"a\": {\"a\": \"Value1\"}, \"b\": {\"a\": \"Value1\"}, \"c\": {\"a\": \"Value1\"}}}", false, false);

        // Get multi path
        assertEquals("[\"{\\\"a\\\":\\\"Value1\\\"}\",\"Value1\",\"Value1\",\"Value1\"]", redisJson.getChildNode("$.multiline..a").toString());
    }

    @Test
    void deleteTest() {
        RedisJson redisJson = new RedisJson();

        // Set root ($)
        redisJson.set("$", "{\"users\": [\"Alice\", \"Bob\"], \"active\": true}", false, false);

        // Delete a field
        int response = redisJson.delete("$.active");
        assertEquals(1, response);
        assertEquals("{\"users\":[\"Alice\",\"Bob\"]}", redisJson.getJson());

        // Delete an array element
        response = redisJson.delete("$.users[0]");
        assertEquals(1, response);
        assertEquals("{\"users\":[\"Bob\"]}", redisJson.getJson());

        // Attempt to delete a non-existent path
        assertEquals(0, redisJson.delete("$.nonExistent"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"users\": [{\"name\": \"Alice\", \"age\": 30}, {\"name\": \"Bob\", \"age\": 25}], \"active\": true, \"multiline\": {\"a\": {\"a\": \"Value1\"}, \"b\": {\"a\": \"Value1\"}, \"c\": {\"a\": \"Value1\"}}}",
        "{\"users\": [{\"name\": \"Alice\", \"age\": 30, \"address\": {\"city\": \"Wonderland\", \"zip\": \"12345\"}}, {\"name\": \"Bob\", \"age\": 25, \"address\": {\"city\": \"Builderland\", \"zip\": \"67890\"}}], \"active\": true}"
    })
    void multiDeleteTest(String json) {
        RedisJson redisJson = new RedisJson();

        // Set root ($)
        redisJson.set("$", json, false, false);

        // Multi-delete operation
        if (json.contains("multiline")) {
            redisJson.delete("$.multiline..a");
            assertEquals("{\"users\":[{\"name\":\"Alice\",\"age\":30},{\"name\":\"Bob\",\"age\":25}],\"active\":true,\"multiline\":{\"b\":{},\"c\":{}}}", redisJson.getJson());
        } else if (json.contains("address")) {
            redisJson.delete("$.users..zip");
            assertEquals("{\"users\":[{\"name\":\"Alice\",\"age\":30,\"address\":{\"city\":\"Wonderland\"}},{\"name\":\"Bob\",\"age\":25,\"address\":{\"city\":\"Builderland\"}}],\"active\":true}", redisJson.getJson());
        }
    }

    @Test
    void getMultiTest() {
        RedisJson redisJson = new RedisJson();

        // Set root ($)
        redisJson.set("$", "{\"a\":2, \"b\": 3, \"nested\": {\"a\": 4, \"b\": null}}", false, false);

        // Multi-get operation
        JsonNode multiGetResult = redisJson.getChildNode("$..b");
        Map<String, JsonNode> res = redisJson.get("$..a", "$..b");

        // Assertions
        assertEquals("[\"3\",\"null\"]", multiGetResult.toString());
        assertEquals("{$..a=[\"2\",\"4\"], $..b=[\"3\",\"null\"]}", res.toString());
    }

    @Test
    void arrayAppendTest() {
        RedisJson redisJson = new RedisJson();

        // Set root ($)
        redisJson.set("$", "{\"users\": [\"Alice\", \"Bob\"]}", false, false);

        // Append to array
        assertEquals(3, redisJson.arrayAppend("$.users", "Charlie"));
        assertEquals(4, redisJson.arrayAppend("$.users", "Dave"));
        

        // Assertions
        assertEquals("{\"users\":[\"Alice\",\"Bob\",\"Charlie\",\"Dave\"]}", redisJson.getJson());
    }

    @Test
    void arrayAppendNestedTest() {
        RedisJson redisJson = new RedisJson();

        // Set root ($)
        redisJson.set("$", "{\"groups\": [{\"name\": \"Group1\", \"members\": [\"Alice\"]}, {\"name\": \"Group2\", \"members\": [\"Bob\"]}]}", false, false);

        // Append to nested array
        redisJson.arrayAppend("$.groups[0].members", "Charlie");
        redisJson.arrayAppend("$.groups[1].members", "Dave");

        // Assertions
        assertEquals("{\"groups\":[{\"name\":\"Group1\",\"members\":[\"Alice\",\"Charlie\"]},{\"name\":\"Group2\",\"members\":[\"Bob\",\"Dave\"]}]}", redisJson.getJson());
    }

}
