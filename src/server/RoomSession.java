package server;

import server.core.GameEngine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RoomSession - represents a single game room with multiple players
 * For simplicity, this implementation uses a single ROOM1
 */
public class RoomSession {
    public final String roomId;
    public final List<ClientHandler> handlers =
            Collections.synchronizedList(new ArrayList<>());
    public final GameEngine engine = new GameEngine();

    public RoomSession(String roomId) {
        this.roomId = roomId;
    }

    public int connectedCount() {
        return handlers.size();
    }
}
