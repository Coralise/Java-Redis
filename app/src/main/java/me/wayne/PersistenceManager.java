package me.wayne;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PersistenceManager {
    
    private static final String FILE_PATH = "resources/persistence/snapshot.dat";

    public static void saveStore() {
        try {
            // Ensure the directory exists
            Files.createDirectories(Paths.get("resources/persistence"));

            // Write object to file
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
                out.writeObject(InMemoryStore.getInstance());
                System.out.println("✅ InMemoryStore saved successfully.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static InMemoryStore loadStore() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            System.out.println("🔄 Loading InMemoryStore snapshot...");
            return (InMemoryStore) in.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("⚠️ No existing snapshot found. Returning a new InMemoryStore.");
            return null; // Return a fresh instance if no snapshot exists
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
