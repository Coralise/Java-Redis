package me.wayne.daos.pubsub;

import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.wayne.CommandHandler;
import me.wayne.daos.io.StorePrintWriter;

public class Subscriber {
    
    @Nonnull
    private final Thread thread;
    private final Consumer<String> consumer;
    
    public Subscriber(Thread thread, StorePrintWriter out, @Nullable UUID requestUuid) {
        this.thread = thread;
        this.consumer = message -> {
            out.print("\r\033[2K");
            out.flush();
            out.println(requestUuid, message);
            out.print(CommandHandler.INPUT_PREFIX);
            out.flush();
        };
    }

    public Thread getThread() {
        return thread;
    }

    public void sendMessage(String message) {
        consumer.accept(message);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((thread == null) ? 0 : thread.hashCode());
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
        Subscriber other = (Subscriber) obj;
        return thread.equals(other.thread);
    }

    

}
