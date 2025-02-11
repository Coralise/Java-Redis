package me.wayne.daos.streams;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import me.wayne.daos.Pair;

public class ConsumerGroup implements Comparable<ConsumerGroup> {

    private static final Random random = new Random();
    private static final Logger LOGGER = Logger.getLogger(ConsumerGroup.class.getName());

    @Nonnull
    final String groupName;

    StreamId lastDeliveredEntryId;

    final Map<String, Pair<Thread, Boolean>> consumers = new LinkedHashMap<>();
    private final TreeSet<StreamId> unreadEntries = new TreeSet<>();
    private final ArrayList<StreamId> pendingEntries = new ArrayList<>();

    public ConsumerGroup(String groupName, StreamId lastDeliveredEntry) {
        this.groupName = groupName;
        this.lastDeliveredEntryId = lastDeliveredEntry;
    }

    public SortedSet<StreamId> getLaterUnreadEntries(StreamId entryId) {
        return new TreeSet<>(unreadEntries.tailSet(entryId, false));
    }

    public void removeUnreadEntry(StreamId entryId) {
        unreadEntries.remove(entryId);
    }

    public boolean hasUnreadEntries() {
        return !unreadEntries.isEmpty();
    }

    public StreamId getUnreadEntry() {
        return unreadEntries.removeFirst();
    }

    public boolean hasPendingEntries() {
        return !pendingEntries.isEmpty();
    }

    public StreamId getPendingEntry() {
        return pendingEntries.remove(0);
    }

    public boolean hasConsumer(String consumerName) {
        return consumers.containsKey(consumerName);
    }

    public boolean hasConsumers() {
        return !consumers.isEmpty();
    }

    public void addConsumer(String consumerName, Thread thread, boolean noAck) {
        LOGGER.log(Level.INFO, "Adding consumer {0} to group {1}", new Object[]{consumerName, groupName});
        consumers.put(consumerName, new Pair<>(thread, noAck));
    }

    public void removeConsumer(String consumerName) {
        LOGGER.log(Level.INFO, "Removing consumer {0} from group {1}", new Object[]{consumerName, groupName});
        consumers.remove(consumerName);
    }
    
    public StreamId getLastDeliveredEntryId() {
        return lastDeliveredEntryId;
    }

    public void setLastDeliveredEntryId(StreamId lastDeliveredEntry) {
        LOGGER.log(Level.INFO, "Setting last delivered entry for group {0} to {1}", new Object[]{groupName, lastDeliveredEntry});
        this.lastDeliveredEntryId = lastDeliveredEntry;
    }
    
    public String getGroupName() {
        return groupName;
    }

    public void feedEntry(StreamEntry streamEntry) {
        lastDeliveredEntryId = streamEntry.getId();
        unreadEntries.add(streamEntry.getId());
        if (!consumers.isEmpty()) {
            int randomIndex = random.nextInt(consumers.size());
            String randomConsumer = (String) consumers.keySet().toArray()[randomIndex];
            Pair<Thread, Boolean> consumerThread = consumers.get(randomConsumer);
            consumerThread.getFirst().interrupt();
            if (Boolean.FALSE.equals(consumerThread.getSecond())) {
                addToPEList(streamEntry.getId());
            }
        }
    }

    public void addToPEList(StreamId entryId) {
        LOGGER.log(Level.INFO, "Adding entry {0} to pending entries list", new Object[]{entryId});
        pendingEntries.add(entryId);
    }
    
    public boolean acknowledgeEntry(StreamId entryId) {
        LOGGER.log(Level.INFO, "Acknowledging entry {0}", entryId);
        LOGGER.log(Level.INFO, "Pending entries size: {0}", pendingEntries.size());
        boolean remove = pendingEntries.remove(entryId);
        LOGGER.log(Level.INFO, "Pending entries size: {0}", pendingEntries.size());
        return remove;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
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
        ConsumerGroup other = (ConsumerGroup) obj;
        return groupName.equals(other.groupName);
    }

    @Override
    public int compareTo(ConsumerGroup o) {
        return groupName.compareTo(o.groupName);
    }
    
}
