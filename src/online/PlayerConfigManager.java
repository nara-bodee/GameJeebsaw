package online;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Manages saving and loading the last used player name for user convenience.
 * Uses a simple .properties file for storage.
 */
public class PlayerConfigManager {

    private static final Path CONFIG_FILE_PATH = Paths.get("player_config.properties");
    private static final String PLAYER_NAME_KEY = "lastPlayerName";

    /**
     * Saves the given player name to the configuration file.
     * @param name The player name to save.
     */
    public static void saveLastPlayerName(String name) {
        Properties props = new Properties();
        if (name != null) {
            props.setProperty(PLAYER_NAME_KEY, name);
        }
        try (OutputStream out = Files.newOutputStream(CONFIG_FILE_PATH)) {
            props.store(out, "GameJeebsaw Player Configuration");
        } catch (IOException e) {
            System.err.println("Error saving player name: " + e.getMessage());
        }
    }

    /**
     * Loads the last saved player name from the configuration file.
     * @return The saved player name, or an empty string if not found or an error occurs.
     */
    public static String loadLastPlayerName() {
        Properties props = new Properties();
        if (!Files.exists(CONFIG_FILE_PATH)) {
            return "";
        }
        try (InputStream in = Files.newInputStream(CONFIG_FILE_PATH)) {
            props.load(in);
            return props.getProperty(PLAYER_NAME_KEY, "");
        } catch (IOException e) {
            System.err.println("Error loading player name: " + e.getMessage());
            return "";
        }
    }
}