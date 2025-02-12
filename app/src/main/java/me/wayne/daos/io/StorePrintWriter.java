package me.wayne.daos.io;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.UUID;

import javax.annotation.Nullable;

public class StorePrintWriter extends PrintWriter {
    
    public StorePrintWriter(OutputStream outputStream) {
        super(outputStream, true);
    }

    public void println(@Nullable UUID uuid, Object message) {
        if (uuid != null) super.println(uuid.toString() + ":" + message);
        else super.println(message);
    }

    public UUID sendCommand(String command) {
        UUID uuid = UUID.randomUUID();
        println(uuid, command);
        return uuid;
    }

}
