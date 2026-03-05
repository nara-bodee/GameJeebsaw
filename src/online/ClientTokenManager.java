package online;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles persistence of a client session token to a local file.
 * This allows the client to attempt reconnection on subsequent startups.
 */
public class ClientTokenManager {

    private static final Path TOKEN_FILE_PATH = Paths.get("token.txt");

    /**
     * Saves a given token to the token.txt file.
     * If the file already exists, it will be overwritten.
     *
     * @param token The session token to save.
     */
    public static void saveToken(String token) {
        try {
            Files.writeString(TOKEN_FILE_PATH, token);
        } catch (IOException e) {
            System.err.println("Error saving token: " + e.getMessage());
        }
    }

    /**
     * Loads the token from token.txt.
     *
     * @return The token as a String, or null if the file doesn't exist or an error occurs.
     */
    public static String loadToken() {
        if (!Files.exists(TOKEN_FILE_PATH)) {
            return null;
        }
        try {
            return Files.readString(TOKEN_FILE_PATH).trim();
        } catch (IOException e) {
            System.err.println("Error loading token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Deletes the token.txt file if it exists.
     */
    public static void deleteToken() {
        try {
            Files.deleteIfExists(TOKEN_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error deleting token: " + e.getMessage());
        }
    }
}