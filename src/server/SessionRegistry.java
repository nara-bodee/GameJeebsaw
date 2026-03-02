package server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SessionRegistry - Maps tokens to player IDs for reconnection
 * Used for handling RECONNECT packets with token validation
 */
public class SessionRegistry {
    private final Map<String, String> tokenToPlayerId = new ConcurrentHashMap<>();

    public void putToken(String token, String playerId) {
        tokenToPlayerId.put(token, playerId);
    }

    public String resolvePlayerId(String token) {
        return tokenToPlayerId.get(token);
    }

    public boolean isValidToken(String token) {
        return tokenToPlayerId.containsKey(token);
    }
}
