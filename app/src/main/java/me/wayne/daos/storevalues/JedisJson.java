package me.wayne.daos.storevalues;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;  
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import me.wayne.AssertUtil;

public class JedisJson implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private JsonNode jsonData;

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(jsonData.toString());
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        String jsonString = (String) in.readObject();
        jsonData = MAPPER.readTree(jsonString);
    }

    public JedisJson() {
        this.jsonData = MAPPER.createArrayNode();
    }

    public String getJson() {
        try {
            return MAPPER.writeValueAsString(jsonData);
        } catch (Exception e) {
            return "[]";
        }
    }

    public String getJson(JsonNode node) {
        try {
            return MAPPER.writeValueAsString(node);
        } catch (Exception e) {
            return "[]";
        }
    }

    public Integer arrayAppend(String path, String... values) {
        JsonNode node = getChildNode(path);
        if (!node.isArray()) return null;
        ArrayNode arrayNode = (ArrayNode) node;
        for (String value : values) {
            arrayNode.add(value);
        }
        return arrayNode.size();
    }

    private static final Pattern INDEX_PATTERN = Pattern.compile("\\[(\\d+)\\]");
    public JsonNode getChildNode(String path) {
        return getChildNode(null, path);
    }

    private ArrayList<String> getNodeRecursively(ArrayList<String> list, String key, JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Entry<String, JsonNode>> fields = objectNode.fields();

            while (fields.hasNext()) {
                Entry<String, JsonNode> entry = fields.next();
                if (entry.getKey().equals(key)) {
                    if (entry.getValue().isValueNode())
                        list.add(entry.getValue().asText());
                    else 
                        list.add(entry.getValue().toString());
                }
                getNodeRecursively(list, key, entry.getValue());
            }
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                getNodeRecursively(list, key, element);
            }
        }
        return list;
    }

    public SortedMap<String, JsonNode> get(String... paths) {
        TreeMap<String, JsonNode> result = new TreeMap<>();
        for (String path : paths) {
            result.put(path, getChildNode(path));
        }
        return result;
    }

    public JsonNode getChildNode(JsonNode node, String path) {

        if (path.startsWith(".")) {
            path = path.substring(1);
            String[] keys = path.split("(?<!\\.)\\.");

            if (keys.length > 1) {
                throw new IllegalArgumentException("ERROR: Multi can only be applied to the last key");
            }

            ArrayList<String> list = new ArrayList<>();
            getNodeRecursively(list, path, node);
            return MAPPER.valueToTree(list);
        } else {
            String[] keys = path.split("(?<!\\.)\\.");
    
            if (node == null) {
                if (keys[0].equals("$")) {
                    if (path.equals("$")) return jsonData;
                    node = jsonData;
                    keys = Arrays.copyOfRange(keys, 1, keys.length);
                    return getChildNode(node, String.join(".", keys));
                } else {
                    throw new IllegalArgumentException("ERROR: Path must start with root ($), was: " + keys[0]);
                }
            }
            
            JsonNode childNode;
            if (node.isObject()) {
                childNode = node.get(keys[0].split("\\[")[0]);
            } else {
                throw new IllegalArgumentException("ERROR: Invalid node type");
            }
            
            Matcher m = INDEX_PATTERN.matcher(keys[0]);
            while (m.find()) {
                if (!childNode.isArray()) {
                    throw new IllegalArgumentException("ERROR: Path " + keys[0] + " contains an index but its node is not an array");
                }
                int index = Integer.parseInt(m.group(1));
                if (index < 0 || index >= childNode.size()) {
                    throw new IllegalArgumentException("ERROR: Index " + index + " is out of bounds for node " + keys[0]);
                }
                childNode = childNode.get(index);
            }
            
            if (childNode == null && !keys[0].isEmpty()) throw new IllegalArgumentException("ERROR: Node " + keys[0] + " is null");
            
            if (keys.length == 1) {
                return childNode;
            } else {
                return getChildNode(childNode, String.join(".", Arrays.copyOfRange(keys, 1, keys.length)));
            }
        }

    }

    public int delete(String path) {
        return delete(null, path, 0);
    }

    public int delete(JsonNode node, String path, int count) {

        String[] keys = path.split("(?<!\\.)\\.");

        if (node == null) {
            if (keys[0].equals("$")) {
                if (path.equals("$")) {
                    jsonData = MAPPER.createArrayNode();
                    return 1;
                } else if (keys.length == 2) {
                    return deleteFromNode(keys[1], jsonData);
                }
                node = jsonData;
                keys = Arrays.copyOfRange(keys, 1, keys.length);
            } else {
                throw new IllegalArgumentException("ERROR: Path must start with root ($)");
            }
        }
        
        JsonNode childNode;
        if (node.isObject()) {
            childNode = node.get(keys[0].split("\\[")[0]);
        } else {
            throw new IllegalArgumentException("ERROR: Invalid node type");
        }
        
        Matcher m = INDEX_PATTERN.matcher(keys[0]);
        while (m.find()) {
            if (!childNode.isArray()) {
                throw new IllegalArgumentException("ERROR: Path " + keys[0] + " contains an index but its node is not an array");
            }
            int index = Integer.parseInt(m.group(1));
            if (index < 0 || index >= childNode.size()) {
                throw new IllegalArgumentException("ERROR: Index " + index + " is out of bounds for node " + keys[0]);
            }
            childNode = childNode.get(index);
        }
        
        if (childNode == null) {
            if (!keys[0].isEmpty()) throw new IllegalArgumentException("ERROR: Node " + keys[0] + " is null");
            else throw new IllegalArgumentException("ERROR: Multi is not supported");
        }

        if (keys.length == 2) {
            return deleteFromNode(keys[1], childNode);
        } else {
            return delete(childNode, String.join(".", Arrays.copyOfRange(keys, 1, keys.length)), count);
        }

    }

    private int deleteFromNode(String key, JsonNode node) {

        if (key.startsWith(".")) {
            
            key = key.substring(1);
            return deleteRecursively(key, node);

        } else {
            Matcher m = INDEX_PATTERN.matcher(key);
            int upIndex = -1;
            while (m.find()) {
                if (!node.isArray()) node = node.get(key.split("\\[")[0]);
                if (upIndex != -1) {
                    node = node.get(upIndex);
                }
                int index = Integer.parseInt(m.group(1));
                if (index < 0 || index >= node.size()) {
                    throw new IllegalArgumentException("ERROR: Index " + index + " is out of bounds for node " + key);
                }
                upIndex = index;
            }
    
            if (node.isObject()) {
                return deleteFromObjectNode(key, (ObjectNode) node);
            } else if (node.isArray()) {
                return deleteFromArrayNode(upIndex, (ArrayNode) node);
            }
            return 0;
        }

    }

    private int deleteRecursively(String key, JsonNode node) {
        int count = 0;

        if (node.isObject()) {

            deleteFromObjectNode(key, (ObjectNode) node);
            count++;

            ObjectNode objectNode = (ObjectNode) node;
            Iterator<String> fieldNames = objectNode.fieldNames();

            while (fieldNames.hasNext()) {
                String field = fieldNames.next();
                count += deleteRecursively(key, objectNode.get(field));
            }

            return count;

        } else if (node.isArray()) {

            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0;i < arrayNode.size();i++) {
                count += deleteRecursively(key, arrayNode.get(i));
            }

            return count;

        } else {
            return count;
        }
    }

    private int deleteFromObjectNode(String key, ObjectNode node) {
        return node.remove(key) != null ? 1 : 0;
    }

    private int deleteFromArrayNode(int index, ArrayNode node) {
        return node.remove(index) != null ? 1 : 0;
    }



    public boolean set(String path, String value, boolean onlyIfExists, boolean onlyIfMissing) {
        
        AssertUtil.assertTrue(path.startsWith("$"), "ERROR: Path must start with root ($)");
        String[] keys = path.split("\\.");
        if (keys.length == 1 && path.equals("$")) {
            try {
                jsonData = MAPPER.readTree(value);
                return true;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return false;
            }
        }

        Pattern indexPattern = Pattern.compile("\\[(\\d+)\\]");

        JsonNode parentNode = null;
        JsonNode childNode = jsonData;
        int lastIndex = -1;

        for (int i = 1;i < keys.length;i++) {

            String key = keys[i];

            if (key.isEmpty()) {
                key = keys[i + 1];
                
                parentNode = childNode;

                try {
                    updateAllKeys(parentNode, key, value);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return false;
                }

                return true;
            }

            lastIndex = -1;
            parentNode = childNode;
            childNode = childNode.get(key.split("\\[")[0]);

            if (childNode == null) {
                if (i == keys.length - 1) break;
                else throw new RuntimeException("ERROR: Node " + key + " is null");
            }

            Matcher m = indexPattern.matcher(key);
            while (m.find()) {
                AssertUtil.assertTrue(childNode.isArray(), "ERROR: Node " + key + " is not an array");
                int index = Integer.parseInt(m.group(1));
                if ((index < 0 || index >= childNode.size()) && m.find()) {
                    throw new RuntimeException("ERROR: Index " + index + " is out of bounds");
                }
                
                parentNode = childNode;
                childNode = childNode.get(index);
                lastIndex = index;
            }

        }

        if (parentNode == null) {
            return false;
        }
        if (onlyIfExists && childNode == null) {
            return false;
        }
        if (onlyIfMissing && childNode != null) {
            return false;
        }

        try {
            if (lastIndex != -1) {
                ArrayNode arrayNode = (ArrayNode) parentNode;
                if (!isNormalString(value)) {
                    if (lastIndex < arrayNode.size()) {
                        arrayNode.set(lastIndex, MAPPER.readTree(value));
                    } else {
                        arrayNode.add(MAPPER.readTree(value));
                    }
                } else {
                    if (lastIndex < arrayNode.size()) {
                        arrayNode.set(lastIndex, value);
                    } else {
                        arrayNode.add(value);
                    }
                }
            } else {
                if (!isNormalString(value)) {
                    ((ObjectNode) parentNode).set(keys[keys.length - 1], MAPPER.readTree(value));
                } else {
                    ((ObjectNode) parentNode).put(keys[keys.length - 1], value);
                }
            }
            return true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }

    }

    private void updateAllKeys(JsonNode node, String keyToUpdate, String newValue) throws JsonProcessingException {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                
                if (entry.getKey().equals(keyToUpdate)) {
                    if (isNormalString(newValue)) {
                        objectNode.put(entry.getKey(), newValue);
                    } else {
                        objectNode.set(keyToUpdate, MAPPER.readTree(newValue));
                    }
                }

                updateAllKeys(entry.getValue(), keyToUpdate, newValue); // Recurse
            }
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                updateAllKeys(element, keyToUpdate, newValue);
            }
        }
    }

    private boolean isNormalString(String str) {
        if (str == null || str.isEmpty()) return true;

        try {
            MAPPER.readTree(str);
            return false; // False if JSON object or array
        } catch (Exception e) {
            return true; // Not valid JSON
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((jsonData == null) ? 0 : jsonData.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JedisJson other = (JedisJson) obj;
        if (jsonData == null) {
            if (other.jsonData != null)
                return false;
        } else if (!jsonData.equals(other.jsonData))
            return false;
        return true;
    }

    
    
}
