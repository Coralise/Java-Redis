package me.wayne.daos.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.UUID;

public class StoreBufferedReader extends BufferedReader {

    public StoreBufferedReader(Reader in) {
        super(in);
    }
    
    public String waitResponse(UUID uuid) throws IOException {
        String line;
        while ((line = readLine()) != null) {
            if (line.startsWith(">> ") && line.startsWith(uuid.toString() + ":", 3)) {
                return line.substring(3 + uuid.toString().length() + 1);
            }
        }
        return uuid.toString() + ":ERR Connection reset";

    }
}
