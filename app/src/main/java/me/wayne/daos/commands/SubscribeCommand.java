package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.pubsub.Channel;

public class SubscribeCommand extends AbstractCommand<String> {

    public SubscribeCommand() {
        super("SUBSCRIBE", 1, false);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        List<String> channels = args;
        
        for (String channelName : channels) {
            Channel channel = store.createOrGetChannel(channelName);
            channel.addSubscriber(Thread.currentThread(), out, requestUuid);
        }

        return OK_RESPONSE;
    }
    
}
