package me.wayne;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.wayne.daos.Config;
import me.wayne.daos.io.StorePrintWriter;

public class PersistenceManager {
    
    private static final Logger LOGGER = Logger.getLogger(PersistenceManager.class.getName());
    private static final Path FOLDER_PATH = Paths.get("resources", "persistence");
    private static boolean aofWritingEnabled = true;
    private static boolean snapshotCountingEnabled = true;

    private static void setAofWritingEnabled(boolean aofWritingEnabled) {
        PersistenceManager.aofWritingEnabled = aofWritingEnabled;
    }

    private static void setSnapshotCountingEnabled(boolean snapshotCountingEnabled) {
        PersistenceManager.snapshotCountingEnabled = snapshotCountingEnabled;
    }

    private PersistenceManager() {}

    public static boolean hasAofLog() {
        return FOLDER_PATH.resolve("aof.log").toFile().exists();
    }

    public static boolean hasSnapshot() {
        return FOLDER_PATH.resolve("snapshot.dat").toFile().exists();
    }

    private static int dataChangedCounter = 0;
    private static long lastSaveTimestamp = System.currentTimeMillis();
    public static void snapshotSaveChecker(int incr) {
        if (!Config.getInstance().isSnapshotsEnabled() || !snapshotCountingEnabled) return;
        dataChangedCounter += incr;
        long lastSaveInMinutes = (System.currentTimeMillis() - lastSaveTimestamp) / 1000 / 60;
        LOGGER.log(Level.INFO, "Data changed counter: {0}", dataChangedCounter);
        LOGGER.log(Level.INFO, "Last save in minutes: {0}", lastSaveInMinutes);
        if (dataChangedCounter >= Config.getInstance().getLeastKeysChangedPerSnapshot()
            && lastSaveInMinutes >= Config.getInstance().getSaveIntervalInMinutes()) {
            LOGGER.log(Level.INFO, "Saving snapshot...");
            saveStore();
        }
    }

    public static void saveStore() {
        try {
            // Ensure the directory exists
            Files.createDirectories(FOLDER_PATH);

            // Write object to file
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FOLDER_PATH.resolve("snapshot.dat").toFile()))) {
                out.writeObject(InMemoryStore.getInstance());
                dataChangedCounter = 0;
                lastSaveTimestamp = System.currentTimeMillis();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static InMemoryStore loadStore() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FOLDER_PATH.resolve("snapshot.dat").toFile()))) {
            return (InMemoryStore) in.readObject();
        } catch (FileNotFoundException e) {
            return null; // Return a fresh instance if no snapshot exists
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void appendToAof(String command) {
        if (!aofWritingEnabled || !Config.getInstance().isAofEnabled()) return;
        try {
            // Ensure the directory exists
            Files.createDirectories(FOLDER_PATH);

            // Write object to file
            try (PrintWriter writer = new PrintWriter(new FileWriter(FOLDER_PATH.resolve("aof.log").toFile(), true), true)) {
                writer.println(command);
                LOGGER.log(Level.INFO, "Appended to AOF: {0}", command);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("all")
    public static void loadAof() {

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(FOLDER_PATH.resolve("aof.log").toFile()))) ) {

            StorePrintWriter out = new StorePrintWriter(System.out);

            setAofWritingEnabled(false);
            setSnapshotCountingEnabled(false);
            String line;
            while ((line = reader.readLine()) != null) {
                InMemoryStore.getInstance().executeCommand(line, out, null);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            setAofWritingEnabled(true);
            setSnapshotCountingEnabled(true);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
