package me.wayne.daos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.wayne.AssertUtil;

public class ConsumerGroup {

    private static final Random random = new Random();
    private static final Logger LOGGER = Logger.getLogger(ConsumerGroup.class.getName());

    final String groupName;

    final String streamKey;

    StreamId lastDeliveredEntryId;

    final ArrayList<StreamId> pendingEntries = new ArrayList<>();

    final Map<String, Thread> consumers = new LinkedHashMap<>();

    public ConsumerGroup(String groupName, String streamKey, StreamId lastDeliveredEntry) {
        this.groupName = groupName;
        this.streamKey = streamKey;
        this.lastDeliveredEntryId = lastDeliveredEntry;
    }

    public void addToPEList(StreamId entryId) {
        LOGGER.log(Level.INFO, "Adding entry {0} to pending entries list for group {1}", new Object[]{entryId, groupName});
        pendingEntries.add(entryId);
    }

    public boolean acknowledgeEntry(StreamId entryId) {
        LOGGER.log(Level.INFO, "Acknowledging entry {0} for group {1}", new Object[]{entryId, groupName});
        LOGGER.log(Level.INFO, "Pending entries size: {0}", pendingEntries.size());
        boolean remove = pendingEntries.remove(entryId);
        LOGGER.log(Level.INFO, "Pending entries size: {0}", pendingEntries.size());
        return remove;
    }

    public boolean hasConsumer(String consumerName) {
        return consumers.containsKey(consumerName);
    }

    public boolean hasConsumers() {
        return !consumers.isEmpty();
    }

    public void addConsumer(String consumerName, Thread thread) {
        LOGGER.log(Level.INFO, "Adding consumer {0} to group {1}", new Object[]{consumerName, groupName});
        consumers.put(consumerName, thread);
    }

    public void removeConsumer(String consumerName) {
        LOGGER.log(Level.INFO, "Removing consumer {0} from group {1}", new Object[]{consumerName, groupName});
        consumers.remove(consumerName);
    }

    public void feedEntry(StreamEntry streamEntry) {
        LOGGER.log(Level.INFO, "Feeding entry {0} to group {1}", new Object[]{streamEntry.getId(), groupName});
        AssertUtil.assertTrue(!consumers.isEmpty(), "No consumers for group " + groupName);
        lastDeliveredEntryId = streamEntry.getId();
        addToPEList(lastDeliveredEntryId);
        int randomIndex = random.nextInt(consumers.size());
        String randomConsumer = (String) consumers.keySet().toArray()[randomIndex];
        Thread consumerThread = consumers.get(randomConsumer);
        consumerThread.interrupt();
    }
    
    public StreamId getLastDeliveredEntryId() {
        return lastDeliveredEntryId;
    }

    public void setLastDeliveredEntryId(StreamId lastDeliveredEntry) {
        LOGGER.log(Level.INFO, "Setting last delivered entry for group {0} to {1}", new Object[]{groupName, lastDeliveredEntry});
        this.lastDeliveredEntryId = lastDeliveredEntry;
    }
    
    public String getStreamKey() {
        return streamKey;
    }
    
    public String getGroupName() {
        return groupName;
    }

    public Map<String, Thread> getConsumers() {
        return new HashMap<>(consumers);
    }
    
}
