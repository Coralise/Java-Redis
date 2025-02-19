package me.wayne.daos;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public class Config {

    private static Config instance;

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private static final String RESOURCE_CONFIG = "config.yml"; // Inside src/main/resources/
    private static final Path USER_CONFIG_PATH = Paths.get("resources", "config.yml"); // Writable path

    private void copyDefaultConfigIfMissing() {
        try {
            Files.createDirectories(USER_CONFIG_PATH.getParent()); // Ensure config folder exists
            
            // Only copy if the user config does not exist
            if (!Files.exists(USER_CONFIG_PATH)) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream(RESOURCE_CONFIG)) {
                    if (in == null) {
                        throw new FileNotFoundException("Default config.yml not found in resources!");
                    }
                    Files.copy(in, USER_CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean snapshotsEnabled;
    private int saveIntervalInMinutes;
    private int leastKeysChangedPerSnapshot;
    private boolean appendOnlyFileEnabled;

    private Config() {
        copyDefaultConfigIfMissing();
        ConfigurationLoader<?> loader = YamlConfigurationLoader.builder()
                .path(USER_CONFIG_PATH)
                .build();

        try {
            ConfigurationNode root = loader.load();
            ConfigurationNode persistenceNode = root.node("Persistence");
            
            snapshotsEnabled = persistenceNode.node("Snapshots", "Enabled").getBoolean(true);
            saveIntervalInMinutes = persistenceNode.node("Snapshots", "Save-Interval-In-Minutes").getInt(5);
            leastKeysChangedPerSnapshot = persistenceNode.node("Snapshots", "Least-Keys-Changed-Per-Snapshot").getInt(10);
            appendOnlyFileEnabled = persistenceNode.node("Append-Only-File", "Enabled").getBoolean(true);
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }
    }

    public boolean isSnapshotsEnabled() {
        return snapshotsEnabled;
    }

    public int getSaveIntervalInMinutes() {
        return saveIntervalInMinutes;
    }

    public int getLeastKeysChangedPerSnapshot() {
        return leastKeysChangedPerSnapshot;
    }

    public boolean isAofEnabled() {
        return appendOnlyFileEnabled;
    }
}
