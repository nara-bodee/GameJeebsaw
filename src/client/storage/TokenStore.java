package client.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * TokenStore - Handles local token persistence for reconnection
 * DEV MAX responsibility: Handle token save/load for client-side reconnection
 */
public class TokenStore {
    private final Path tokenPath;

    public TokenStore(String fileName) {
        this.tokenPath = Path.of(fileName);
    }

    /**
     * Save token to local file
     */
    public void saveToken(String token) throws IOException {
        if (token == null || token.trim().isEmpty()) {
            Files.writeString(tokenPath, "");
        } else {
            Files.writeString(tokenPath, token.trim());
        }
    }

    /**
     * Load token from local file
     */
    public String loadToken() throws IOException {
        if (!Files.exists(tokenPath)) {
            return null;
        }
        String t = Files.readString(tokenPath).trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Clear stored token
     */
    public void clearToken() throws IOException {
        Files.writeString(tokenPath, "");
    }

    /**
     * Check if token exists
     */
    public boolean hasToken() throws IOException {
        String t = loadToken();
        return t != null && !t.isEmpty();
    }
}
