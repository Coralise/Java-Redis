package me.wayne.daos;

import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class Config {

    public Config() {
        loadConfig();
    }

    private void loadConfig() {
        Yaml yaml = new Yaml();
        try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
            Map<String, Object> config = yaml.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
