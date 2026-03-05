package client;

/**
 * ClientContext - Holds current client session information
 */
public class ClientContext {
    public String playerId;      // P1, P2, P3
    public String token;         // Session token
    public String roomId;        // Room ID
    public String playerName;    // Display name

    public boolean isConnected = false;

    @Override
    public String toString() {
        return "ClientContext{" +
                "playerId='" + playerId + '\'' +
                ", token='" + token + '\'' +
                ", roomId='" + roomId + '\'' +
                ", playerName='" + playerName + '\'' +
                ", isConnected=" + isConnected +
                '}';
    }
}
