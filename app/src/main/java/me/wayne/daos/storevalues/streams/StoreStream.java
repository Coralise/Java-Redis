package me.wayne.daos.storevalues.streams;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nullable;

import me.wayne.daos.Printable;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.PrintableList;

public class StoreStream implements Serializable, Printable {
    private static final long serialVersionUID = 1L;

    private final String storeKey;
    private final TreeSet<StreamEntry> entries = new TreeSet<>();
    private final TreeSet<ConsumerGroup> consumerGroups = new TreeSet<>();
    private transient ArrayList<Thread> readers;

    public StoreStream(String key) {
        this.storeKey = key;
        readers = new ArrayList<>();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        readers = new ArrayList<>();
    }

    public void addReader(Thread reader) {
        readers.add(reader);
    }

    public void removeReader(Thread reader) {
        readers.remove(reader);
    }

    public String add(String id, List<String> fieldsAndValues) {
        
        StreamId streamId = new StreamId(id);
        while (hasEntry(streamId)) streamId = new StreamId(StreamId.incrementIdSequence(id));
        StreamEntry streamEntry = new StreamEntry(streamId, fieldsAndValues);
        entries.add(streamEntry);
        
        for (ConsumerGroup consumerGroup : consumerGroups) {
            consumerGroup.feedEntry(streamEntry);
        }

        for (Thread reader : readers) reader.interrupt();

        return streamEntry.getId().toString();
    }

    public SortedSet<StreamEntry> getEntries() {
        return entries;
    }

    public boolean hasEntry(StreamId entryId) {
        return entries.floor(new StreamEntry(entryId)) != null && entries.floor(new StreamEntry(entryId)).getId().equals(entryId);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    public StoreStream read(StorePrintWriter out, UUID requestUuid, String lastReadId, int count, Long block) {

        if (block == null) {
            return range(lastReadId, false, "+", true, count);
        } else {
            
            out.println(requestUuid, "Waiting for streams for " + block + " milliseconds...");
            addReader(Thread.currentThread());
            int currentCount = 0;
            while (currentCount < (count <= 0 ? 1 : count)) {
                try {
                    Thread.sleep(block);
                    break;
                } catch (InterruptedException e) {
                    out.println(requestUuid, getLastEntry().toPrint(0));
                    currentCount++;
                }
            }
            removeReader(Thread.currentThread());
            return new StoreStream(storeKey);
                
        }

    }

    public StoreStream range(String startIdString, boolean startInclusive, String endIdString, boolean endInclusive, int count) {
        StreamId startId = new StreamId(startIdString);
        StreamId endId = new StreamId(endIdString);
        StoreStream subStream = new StoreStream(storeKey);
        NavigableSet<StreamEntry> subSet = entries.subSet(new StreamEntry(startId, null), startInclusive, new StreamEntry(endId, null), endInclusive);
        int currentCount = 0;
        for (StreamEntry entry : subSet) {
            if (count > 0 && currentCount >= count) break;
            subStream.add(entry.getId().toString(), entry.getFieldsAndValues());
            currentCount++;
        }
        return subStream;
    }

    @Nullable
    public StreamEntry getFirstEntry() {
        return entries.isEmpty() ? null : entries.first();
    }

    @Nullable
    public StreamEntry getLastEntry() {
        return entries.isEmpty() ? null : entries.last();
    }

    public String createConsumerGroup(String group, String id) {
        if (consumerGroups.contains(new ConsumerGroup(group, null))) return "-BUSYGROUP";
        if (id.equals("$")) {
            StreamEntry lastEntry = getLastEntry();
            id = lastEntry == null ? "0-0" : lastEntry.getId().toString();
        }
        ConsumerGroup consumerGroup = new ConsumerGroup(group, new StreamId(id));
        consumerGroups.add(consumerGroup);
        return "OK";
    }

    public void readGroup(PrintWriter out, String group, String consumerName, int count, Long block, boolean noAck, String id) {
        ConsumerGroup consumerGroup = getConsumerGroupByGroupName(group);
        if (consumerGroup == null) {
            out.println("NOGROUP No such consumer group");
            return;
        }
        if (consumerGroup.hasConsumer(consumerName)) {
            out.println("BUSYGROUP Consumer name already exists");
            return;
        }

        if (id.equals("$")) {
            StreamEntry lastEntry = getLastEntry();
            id = lastEntry == null ? "0-0" : lastEntry.getId().toString();
        }
        SortedSet<StreamId> unreadEntries = consumerGroup.getLaterUnreadEntries(new StreamId(id));
        
        int currentCount = 0;
        while (currentCount < (count <= 0 ? 1 : count) && !unreadEntries.isEmpty()) {
            StreamId entryId = unreadEntries.removeFirst();
            consumerGroup.removeUnreadEntry(entryId);
            StreamEntry entry = entries.floor(new StreamEntry(entryId));
            if (entry == null || !entry.getId().equals(entryId)) continue;

            out.println(entry.toPrint(0));
            currentCount++;
            if (!noAck) {
                consumerGroup.addToPEList(entry.getId());
            }
        }

        if (block != null && currentCount < (count <= 0 ? 1 : count)) {
            consumerGroup.addConsumer(consumerName, Thread.currentThread(), noAck);
            while (currentCount < (count <= 0 ? 1 : count)) {
                try {
                    Thread.sleep(block);
                    break;
                } catch (InterruptedException e) {
                    StreamId entryId = consumerGroup.getUnreadEntry();
                    StreamEntry entry = entries.floor(new StreamEntry(entryId));
                    if (entry == null || !entry.getId().equals(entryId)) continue;
                    out.println(entry.toPrint(0));
                    currentCount++;
                }
            }
            consumerGroup.removeConsumer(consumerName);
        }

    }

    @Nullable
    public ConsumerGroup getConsumerGroupByGroupName(String groupName) {
        ConsumerGroup floor = consumerGroups.floor(new ConsumerGroup(groupName, null));
        return floor != null && floor.getGroupName().equals(groupName) ? floor : null;
    }

    @Override
    public String toPrint(int indent) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("1) " + storeKey + "\n");
        stringBuilder.append("2) " + new PrintableList<>(entries, indent + 1));
        return stringBuilder.toString();
    }

}