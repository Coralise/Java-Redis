package me.wayne.daos.pubsub;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import me.wayne.InMemoryStore;
import me.wayne.daos.io.StorePrintWriter;

public class Channel {

    private final String name;
    private final Set<Subscriber> subscribers = ConcurrentHashMap.newKeySet();

    public Channel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addSubscriber(Thread subscriber, StorePrintWriter out, @Nullable UUID requestUuid) {
        subscribers.add(new Subscriber(subscriber, out, requestUuid));
    }

    public boolean removeSubscriber(Thread subscriber) {
        boolean res = subscribers.remove(new Subscriber(subscriber, null, null));
        if (subscribers.isEmpty()) InMemoryStore.getInstance().removeChannel(name);
        return res;
    }

    public int publishMessage(String message) {
        int count = 0;
        for (Subscriber subscriber : subscribers) {
            subscriber.sendMessage(message);
            count++;
        }
        return count;
    }
    
}
