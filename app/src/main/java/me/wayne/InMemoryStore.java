package me.wayne;

import java.util.HashMap;
import java.util.Map;

import me.wayne.daos.ConsumerGroup;

public class InMemoryStore {

    private final Map<String, Object> store = new HashMap<>();

    // Key: Sorted set key, Value: Map of member to score
    private final Map<String, Map<String, Integer>> treeSetMembers = new HashMap<>();

    // Key: Consumer group name, Value: ConsumerGroup object
    private final Map<String, ConsumerGroup> consumerGroups = new HashMap<>();
    // Key: Stream key, Value: Consumer group name
    private final Map<String, String> streamConsumerMap = new HashMap<>();

    public Map<String, Object> getStore() {
        return store;
    }

    public Map<String, Map<String, Integer>> getTreeSetMembers() {
        return treeSetMembers;
    }
    
    public void addConsumerGroup(ConsumerGroup consumerGroup) {
        consumerGroups.put(consumerGroup.getGroupName(), consumerGroup);
        streamConsumerMap.put(consumerGroup.getStreamKey(), consumerGroup.getGroupName());
    }

    public ConsumerGroup getConsumerGroupByGroupName(String groupName) {
        return consumerGroups.get(groupName);
    }

    public ConsumerGroup getConsumerGroupByStreamKey(String streamKey) {
        return consumerGroups.get(streamConsumerMap.get(streamKey));
    }

    public boolean hasCustomerGroup(String groupName) {
        return consumerGroups.containsKey(groupName);
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(String key, Class<T> clazz) {
        Object obj = store.get(key);
        if (obj == null) {
            return null;
        }
        AssertUtil.assertTrue(clazz.isInstance(obj), "Value is not of the expected type (" + clazz.getSimpleName() + ")");
        return (T) obj;
    }

}