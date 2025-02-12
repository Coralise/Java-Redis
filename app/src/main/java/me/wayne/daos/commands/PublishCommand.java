package me.wayne.daos.commands;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.pubsub.Channel;

public class PublishCommand extends AbstractCommand<Integer> {

    public PublishCommand() {
        super("PUBLISH", 2, 2);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, List<String> args) {
        String channelName = args.get(0);
        String message = args.get(1);
        
        Channel channel = store.getChannel(channelName);

        if (channel == null) return 0;

        return channel.publishMessage(message);
    }
    
}
